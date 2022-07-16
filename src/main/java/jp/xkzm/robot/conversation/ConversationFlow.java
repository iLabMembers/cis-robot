package jp.xkzm.robot.conversation;

import java.util.*;

public class ConversationFlow {
   private static final long                CONVERSATION_TIME_LIMIT = 4 * 60 * 1000;  // msec.

   private static long                      CONVERSATION_BEGIN_TIME;  // msec.
   private static ConversationFlow          cf;

   private final Set<ConversationContent>   startingPoints;
   private final List<ConversationContent>  flow;

   private boolean                 endConversation;
   private ConversationFlowSetting setting;
   private ConversationContent     introduction;
   private ConversationContent     conclusion;

   private ConversationFlow() {

      this.introduction    = null;
      this.conclusion      = null;
      this.startingPoints  = new HashSet<>();
      this.flow            = new ArrayList<>();
      this.endConversation = false;

   }

   public static ConversationFlow getConversationFlow() {

      if (cf == null) cf = new ConversationFlow();

      return cf;

   }

   public static boolean createdConversationFlow() {

      return cf != null;

   }

   public ConversationFlow setIntroduction(ConversationContent introduction) {

      if (this.introduction != null) {

         System.out.println("Already set a conversation topic about introduction.");

         return cf;

      }

      this.introduction = introduction;

      checkReady();

      return cf;

   }

   public ConversationFlow setConclusion(ConversationContent conclusion) {

      this.conclusion = conclusion;

      checkReady();

      return cf;

   }

   public ConversationFlow appendStartPoint(ConversationContent startConversationContent) {

      startConversationContent.setStart();
      startingPoints.add(startConversationContent);
      checkReady();

      return cf;

   }

   private void decideStartPoint() {

      // random
      this.flow.add(startingPoints.stream().sorted(new RandomComparator<>()).iterator().next());

   }

   private boolean checkReady() {

      boolean isnullIntroduction   = introduction          != null;
      boolean isnullConclusion     = conclusion            != null;
      boolean isnullStartingPoints = startingPoints.size() != 0;

      return isnullIntroduction && isnullConclusion && isnullStartingPoints;

   }

   public void startConversation() {

      introduction.startConversation();

      decideStartPoint();

      ConversationFlow.CONVERSATION_BEGIN_TIME = System.nanoTime();

      Iterator<ConversationContent> cfItr = flow.iterator();
      while (cfItr.hasNext() && !endConversation) {

          cfItr.next().startConversation();

      }

      conclusion.startConversation();

   }

   public boolean checkReadyAllConversation() {

      if (! introduction.checkReady()) {

         System.err.println(String.format("[%s key = %s] is not ready.",
                 introduction.getClass().getSimpleName(),
                 introduction.getKey()
         ));
         return false;

      }

      for (ConversationContent conv: getConversationFlow().startingPoints) {

         if (! conv.checkReady()) {

            System.err.println(String.format("[%s key = %s] is not ready.",
                    conv.getClass().getSimpleName(),
                    conv.getKey()
            ));
            return false;

         }

      }

      if (! conclusion.checkReady()) {

         System.err.println(String.format("[%s key = %s] is not ready.",
                 conclusion.getClass().getSimpleName(),
                 conclusion.getKey()
         ));
         return false;

      }

      return true;

   }


   private static final class RandomComparator<T> implements Comparator<T> {

      private final Map<T, Integer> map = new IdentityHashMap<>();
      private final Random random;

      public RandomComparator() {

         this(new Random());

      }

      public RandomComparator(Random random) {

         this.random = random;

      }

      @Override
      public int compare(T t1, T t2) {

         return Integer.compare(valueFor(t1), valueFor(t2));

      }

      private int valueFor(T t) {

         synchronized (map) {

            return map.computeIfAbsent(t, ignore -> random.nextInt());

         }

      }

   }

}
