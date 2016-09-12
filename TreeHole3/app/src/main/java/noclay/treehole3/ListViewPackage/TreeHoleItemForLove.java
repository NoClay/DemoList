package noclay.treehole3.ListViewPackage;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobPointer;
import noclay.treehole3.OtherPackage.SignUserBaseClass;

/**
 * Created by 寒 on 2016/7/19.
 */
public class TreeHoleItemForLove extends BmobObject{
    private String fromUserName;//表白的来源
    private String toUserName;//表白的对象
    private Boolean isFromMan;//表白是否来自男性
    private SignUserBaseClass author;//表白的作者
    private String content;//表白的内容
    private Integer likesNumber = 0;//对于表白祝福的个数
    private List<String> likesUserList = new ArrayList<>();//表示祝福的用户的信息表，仅含手机号

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    public Boolean getFromMan() {
        return isFromMan;
    }

    public void setFromMan(Boolean fromMan) {
        isFromMan = fromMan;
    }


    public SignUserBaseClass getAuthor() {
        return author;
    }

    @Override
    public String getCreatedAt() {
        return super.getCreatedAt();
    }

    public void setAuthor(SignUserBaseClass author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikesNumber() {
        return likesUserList.size();
    }

    public void addLikes(String phoneNumber){
        likesUserList.add(phoneNumber);
        likesNumber = likesUserList.size();
    }

    public boolean isLiked(String phoneNumber){//判断当前用户是否喜欢
        return likesUserList.contains(phoneNumber);
    }

}
