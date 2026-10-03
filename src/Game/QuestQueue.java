package Game;

import java.util.HashSet;
import java.util.PriorityQueue;

/** Quest queue that refuses quests the owner has muted. Player characters swap their two queues for this subclass,
 *  so the ~50 places that post quests need no changes and a deleted quest stays deleted when its trigger fires again. */
public class QuestQueue extends PriorityQueue<Quest> {
    static final long serialVersionUID = 1L;
    HashSet<String> muted; // shared with the owner's PlayerControl.muted

    public QuestQueue(PriorityQueue<Quest> from, HashSet<String> muted) {
        super(20);
        this.muted = muted;
        if (from != null) addAll(from);
    }

    @Override
    public boolean offer(Quest q) {
        if (q != null && muted != null && !q.userCreated && muted.contains(q.name)) return false;
        return super.offer(q);
    }
}
