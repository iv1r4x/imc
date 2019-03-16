package com.h3c.imc.syslog.templet.view;

import com.h3c.imc.syslog.entity.SyslogTempletEntity;

public abstract interface SyslogTempletSelectIf
{
  public abstract void setTempletContent(String paramString, int paramInt);
  
  public abstract void setTempletEntity(SyslogTempletEntity paramSyslogTempletEntity, int paramInt);
}

/* Location:
 * Qualified Name:     com.h3c.imc.syslog.templet.view.SyslogTempletSelectIf
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */