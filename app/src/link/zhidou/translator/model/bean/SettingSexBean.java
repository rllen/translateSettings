package link.zhidou.translator.model.bean;

/**
 * Creating Time :  2017/9/11.
 * AUTHOR : CaoQi
 * E-MAIL : 18507118810@163.com
 * Description : This is a Class for ----
 */

public class SettingSexBean {


    private String label;
    private String value;
    private boolean isShow;
    public SettingSexBean() {

    }

    public SettingSexBean(String label, String value, boolean isShow) {
        this.label = label;
        this.value = value;
        this.isShow = isShow;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    @Override
    public String toString() {
        return "SettingSexBean{" +
                "label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", isShow=" + isShow +
                '}';
    }
}
