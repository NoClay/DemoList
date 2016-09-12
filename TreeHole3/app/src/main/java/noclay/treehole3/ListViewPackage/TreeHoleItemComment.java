package noclay.treehole3.ListViewPackage;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobPointer;
import noclay.treehole3.OtherPackage.SignUserBaseClass;

/**
 * Created by 寒 on 2016/7/22.
 */
public class TreeHoleItemComment extends BmobObject{
    private String content;//评论的内容
    private TreeHoleItemForSpeak post;//评论的吐槽
    private SignUserBaseClass author;//评论的作者
    private String authorName;//评论者的昵称

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public TreeHoleItemForSpeak getPost() {
        return post;
    }

    public void setPost(TreeHoleItemForSpeak post) {
        this.post = post;
    }

    public SignUserBaseClass getAuthor() {
        return author;
    }

    public void setAuthor(SignUserBaseClass author) {
        this.author = author;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
