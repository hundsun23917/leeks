
package bean;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("net.hexar.json2pojo")
@SuppressWarnings("unused")
public class FxhResult {

    @SerializedName("code")
    private Long mCode;
    @SerializedName("currpage")
    private Long mCurrpage;
    @SerializedName("data")
    private List<Datum> mData;
    @SerializedName("maxpage")
    private Long mMaxpage;
    @SerializedName("msg")
    private String mMsg;

    public Long getCode() {
        return mCode;
    }

    public void setCode(Long code) {
        mCode = code;
    }

    public Long getCurrpage() {
        return mCurrpage;
    }

    public void setCurrpage(Long currpage) {
        mCurrpage = currpage;
    }

    public List<Datum> getData() {
        return mData;
    }

    public void setData(List<Datum> data) {
        mData = data;
    }

    public Long getMaxpage() {
        return mMaxpage;
    }

    public void setMaxpage(Long maxpage) {
        mMaxpage = maxpage;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

}
