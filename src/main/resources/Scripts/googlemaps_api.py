import pandas as pd
import numpy as np
import requests
import sys
import json

# pip install -U googlemaps
import googlemaps
import pprint # list型やdict型を見やすくprintするライブラリ

def save_json(dict,PATH):
    """
    jsonに保存する関数
    第一引数に辞書型を入れ，第二引数にパスの名前を入れる
    """
    f = open(PATH, "w")
    json.dump(dict, f, ensure_ascii=False, indent=4, sort_keys=True, separators=(',', ': '))


args = sys.argv

if len(args) <= 1:
    sys.exit("pleace input API KEY!!!")

key =  args[1] # 上記で作成したAPIキーを入れる
# コマンド：python googlemaps_api.py apiのkey

# 観光地の名前リストをCSVから抽出
SightBasic_DF = pd.read_csv("../data/20220719_日本科学未来館_SightBasic.csv")
name_ls = list(SightBasic_DF["SightName"])

#インスタンス生成
client = googlemaps.Client(key) 
#================================================================================================================
#geocodeデータを保存
res_geocode_dict = {}
for name_i in name_ls:
    geocode_result = client.geocode(name_i,language='ja' ) # 観光地の位置情報を検索
    res_geocode_dict[name_i] = geocode_result
#JSONとして出力
GEOCODE_PATH = '../data/res_yosen_geocode.json'
save_json(res_geocode_dict,GEOCODE_PATH)

#================================================================================================================
#日本科学未来館の周辺情報を取り出す(JSONに出力)
miraikan_result = client.geocode('日本科学未来館',language='ja' ) # 東京未来館の位置情報を検索
MIRAIKAN_PATH = '../data/res_miraikan.json'
save_json(miraikan_result,MIRAIKAN_PATH)
#================================================================================================================
#各地点の周辺情報を保存(保留)

# place_result = client.places_nearby(location=loc_kan, radius=200, type='food',language='ja' ) #半径200m以内のレストランの情報を取得
# pprint.pprint(place_result)

#================================================================================================================
#場所の情報を保存
res_place_info_dict = {}
for name_i in name_ls:
    place_informations = client.places(name_i,language='ja') # 観光地の位置情報を検索
    res_place_info_dict[name_i] = place_informations
#JSONとして出力
PLACE_INFO_PATH = '../data/res_yosen_place_info.json'
save_json(res_place_info_dict,PLACE_INFO_PATH)

#================================================================================================================
# 東京未来館から観光地まで移動方法・所要時間をを取得
res_directions_info_dict = {}
loc_miraikan = miraikan_result[0]["geometry"]["location"] #日本未来館の緯度経度を取得

for name_i in name_ls:
    trg_place_dict = res_geocode_dict[name_i]
    trg_place_geocode = trg_place_dict[0]["geometry"]["location"]
    place_directons= client.directions(origin=loc_miraikan, destination=trg_place_geocode,language= 'ja') 
    res_directions_info_dict[name_i] = place_directons
#JSONとして出力
PLACE_DIRECTIONS_PATH = '../data/res_yosen_directions_info.json'
save_json(res_directions_info_dict,PLACE_DIRECTIONS_PATH)
