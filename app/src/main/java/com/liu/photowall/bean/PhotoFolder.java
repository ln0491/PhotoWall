package com.liu.photowall.bean;

/**
 * Created by 刘楠
 */
public class PhotoFolder {

    //文件夹路径
    private String path;
    //文件夹名称
    private String name;
    //第一张图片的路径
    private String firstImagePath;
    //图片数量
    private int imageCount;

    public PhotoFolder() {
    }

    public PhotoFolder(String path, String name, String firstImagePath, int imageCount) {
        this.path = path;
        this.name = name;
        this.firstImagePath = firstImagePath;
        this.imageCount = imageCount;
    }

    public void setPath(String path) {
        this.path = path;
        int lastIndexOf = this.path.lastIndexOf("/")+1;
        this.name=this.path.substring(lastIndexOf);
    }


    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public int getImageCount() {
        return imageCount;
    }
}
