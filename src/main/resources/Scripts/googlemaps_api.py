import pandas as pd
import numpy as np
import requests
import sys
# pip install -U googlemaps
import googlemaps
import pprint # list型やdict型を見やすくprintするライブラリ

args = sys.argv
key =  args[1] # 上記で作成したAPIキーを入れる
# python googlemaps_api.py apiのkey

client = googlemaps.Client(key) #インスタンス生成

geocode_result = client.geocode('船の科学館',language='ja' ) # 観光地の位置情報を検索
loc_kan = geocode_result[0]['geometry']['location']# 観光地の軽度・緯度の情報のみ取り出す

pprint.pprint(geocode_result)

miraikan_result = client.geocode('日本科学未来館',language='ja' ) # 東京未来館の位置情報を検索
loc_mirai= geocode_result[0]['geometry']['location']# 東京未来館の軽度・緯度の情報のみ取り出す

pprint.pprint(miraikan_result)

place_result = client.places_nearby(location=loc_kan, radius=200, type='food',language='ja' ) #半径200m以内のレストランの情報を取得

pprint.pprint(place_result)

place_informations= client.places("船の科学館",language= 'ja') # 観光地の具体的な情報を検索

pprint.pprint(place_informations)

place_directons= client.directions(origin=loc_mirai, destination=loc_kan,language= 'ja') # 東京未来館から観光地まで移動方法・所要時間をを取得

pprint.pprint(place_directons)