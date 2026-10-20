package entity;
//输出图片任务的json类（图片以base_64编码输出，在端侧解码）
public class OutputTask {
    private String fileName;
    private String fileType;
    private long fileSize;  // 单位kb
    private String base_64;
    
    public OutputTask() {
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getBase_64() {
        return base_64;
    }
    
    public void setBase_64(String base_64) {
        this.base_64 = base_64;
    }
}
