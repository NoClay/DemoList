package noclay.treehole3.ListViewPackage;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobPointer;
import noclay.treehole3.OtherPackage.SignUserBaseClass;

/**
 * Created by 寒 on 2016/7/20.
 */
public class TreeHoleItemForSpeak extends BmobObject {
    private String content;//吐槽的内容
    private SignUserBaseClass author;//指向作者
    private Boolean isNoName;
    private List<String> likeList = new ArrayList<>();//喜欢的人的列表
    private List<String> disLikeList = new ArrayList<>();//讨厌的列表
    private Integer sharedNumber;//分享的次数
    private Integer commentNumber;//评论的个数


    public Boolean getNoName() {
        return isNoName;
    }

    public void setNoName(Boolean noName) {
        isNoName = noName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public SignUserBaseClass getAuthor() {
        return author;
    }

    public void setAuthor(SignUserBaseClass author) {
        this.author = author;
    }

    public List<String> getLikeList() {
        return likeList;
    }

    public boolean isLike(String phoneNumber){
        return likeList.contains(phoneNumber);
    }
    public boolean isDisLike(String phoneNumber){
        return disLikeList.contains(phoneNumber);
    }
    public int getAdmireNumber(){
        return likeList.size() - disLikeList.size();
    }

    public void addLike(String phoneNumber) {
        likeList.add(phoneNumber);
        disLikeList.remove(phoneNumber);
    }

    public void addDisLike(String phoneNumber){
        likeList.remove(phoneNumber);
        disLikeList.add(phoneNumber);
    }

    public void setLikeList(List<String> likeList) {
        this.likeList = likeList;
    }

    public List<String> getDisLikeList() {
        return disLikeList;
    }

    public void setDisLikeList(List<String> disLikeList) {
        this.disLikeList = disLikeList;
    }

    public Integer getSharedNumber() {
        return sharedNumber;
    }

    public void setSharedNumber(Integer sharedNumber) {
        this.sharedNumber = sharedNumber;
    }


    public Integer getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(Integer commentNumber) {
        this.commentNumber = commentNumber;
    }
}