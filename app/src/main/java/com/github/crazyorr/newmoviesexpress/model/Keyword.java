package com.github.crazyorr.newmoviesexpress.model;

import net.sourceforge.pinyin4j.PinyinHelper;

/**
 * Created by wanglei02 on 2015/11/9.
 */
public class Keyword implements Comparable<Keyword>{
    private String value;
    private int categoryIndex;
    private String mCompareString;

    public Keyword() {
        this.value = "";
    }

    public Keyword(String value) {
		this.value = value;
	}

	public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getCategoryIndex() {
        return categoryIndex;
    }

    public void setCategoryIndex(int categoryIndex) {
        this.categoryIndex = categoryIndex;
    }

    private boolean isCharacterChinese(char c){
        return Character.toString(c).matches("[\u4E00-\u9FA5]+");
    }

    protected String toCompareString() {
        if(mCompareString == null){
            StringBuilder sb = new StringBuilder();
            if(value != null){
                char[] chars = value.trim().toCharArray();
                for (int i = 0; i < chars.length; i++) {
                    if (isCharacterChinese(chars[i])) {
                        sb.append(PinyinHelper.toHanyuPinyinStringArray(chars[i])[0].charAt(0));
                    }else{
                        sb.append(chars[i]);
                    }
                }
            }
            mCompareString = sb.toString();
        }
        return mCompareString;
    }

    public char getFirstLetter(){
        String str = toCompareString();
        return str.length() > 0 ? str.charAt(0) : ' ';
    }

    @Override
    public int compareTo(Keyword another) {
        return toCompareString().compareTo(another.toCompareString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Keyword keyword = (Keyword) o;

        if (categoryIndex != keyword.categoryIndex) return false;
        return value.equals(keyword.value);

    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + categoryIndex;
        return result;
    }
}
