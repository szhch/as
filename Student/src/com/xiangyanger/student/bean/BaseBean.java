package com.xiangyanger.student.bean;

import java.io.Serializable;

public class BaseBean implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -2355228155974981931L;
    
    public int status;
    public String info;
    
    public int getStatus(){
        return status;
    }
    
    public void setStatus(int status){
        this.status = status;
    }
    
    public String getInfo(){
        return info;
    }
    
    public void setInfo(String info){
        this.info = info;
    }

}
