package cmdline;

import common.*;
import common.comment.CommentsCollector;
import common.like.LikesCollector;
import common.page.PageCollector;
import common.post.Post;
import common.post.PostsCollector;

import java.util.ArrayList;
import java.util.List;

public class DataCollector
{
    public static void main(String[] args) throws Exception
    {
        Config.init();

        int scrapeCount = 0;

        boolean fetch = true;

        while (fetch)
        {
            System.out.println(Util.getDbDateTimeEst() + " started fetching data from " + Config.since + " until " + Config.until);

            collectData();

            scrapeCount++;

            System.out.println(Util.getDbDateTimeEst() + " scraped " + scrapeCount + " time(s)");

            Config.init();

            if(Config.numOfScrapes != 0 && scrapeCount >= Config.numOfScrapes)
            {
                fetch = false;
            }
        }
    }

    public static void collectData()
    {
        for(String username: Config.pages)
        {
            PageCollector pageCollector = new PageCollector(username);
            pageCollector.collect();
        }

        List<Post> posts = new ArrayList<Post>();
        for(String username: Config.pages)
        {
            PostsCollector postsCollector = new PostsCollector(username, Config.since, Config.until);
            postsCollector.collect();
            posts.addAll(postsCollector.getPosts());
            System.out.println(Util.getDbDateTimeEst() + " fetched " + postsCollector.getPosts().size() + " posts from page " + username);
            Util.sleepMillis(postsCollector.getPosts().size() * 100);
        }
        System.out.println(Util.getDbDateTimeEst() + " fetched total " + posts.size() + " posts from " + Config.pages.size() + " pages");

        if(Config.collectComments)
        {
            collectComments(posts);
        }

        if(Config.collectLikes)
        {
            collectLikes(posts);
        }
    }

    public static void collectComments(List<Post> posts)
    {
        for(Post post: posts)
        {
            System.out.println(Util.getDbDateTimeEst() + " fetching comments for post " + post.getId() + " created_at " + post.getCreatedAt());
            CommentsCollector commentsCollector = new CommentsCollector(post.getUsername(), post.getId());
            commentsCollector.collect();
            System.out.println(Util.getDbDateTimeEst() + " fetched " + commentsCollector.getCommentIds().size() + " comments");
            Util.sleepMillis(commentsCollector.getCommentIds().size() * 100);

            for(String commentId: commentsCollector.getCommentIds())
            {
                System.out.println(Util.getDbDateTimeEst() + " fetching replies for comment " + commentId);
                CommentsCollector repliesCollector = new CommentsCollector(post.getUsername(), post.getId(), commentId);
                repliesCollector.collect();
                System.out.println(Util.getDbDateTimeEst() + " fetched " + repliesCollector.getCommentIds().size() + " replies");
                Util.sleepMillis(repliesCollector.getCommentIds().size() * 100);
            }
        }
    }

    public static void collectLikes(List<Post> posts)
    {
        for(Post post: posts)
        {
            System.out.println(Util.getDbDateTimeEst() + " fetching likes for post " + post.getId() + " created_at " + post.getCreatedAt());
            LikesCollector likesCollector = new LikesCollector(post.getUsername(), post.getId());
            likesCollector.collect();
            System.out.println(Util.getDbDateTimeEst() + " fetched " + likesCollector.likes.size() + " likes");
            Util.sleepMillis(likesCollector.likes.size() * 100);
        }
    }
}