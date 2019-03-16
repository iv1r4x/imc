package com.h3c.imc.syslog.templet.view;

import com.h3c.imc.common.StringManager;
import com.h3c.imc.faces.entity.OperateResult;
import com.h3c.imc.syslog.SyslogException;
import com.h3c.imc.syslog.common.view.Operlog;
import com.h3c.imc.syslog.common.view.SyslogViewUtils;
import com.h3c.imc.syslog.entity.RuleParam;
import com.h3c.imc.syslog.entity.SyslogTempletEntity;
import com.h3c.imc.syslog.entity.SyslogTempletParaEntity;
import com.h3c.imc.syslog.templet.func.SyslogCommonTempletMgr;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import org.apache.commons.lang.StringUtils;

public class SyslogCommonTempConfigBean
{
  protected static final StringManager SM = StringManager.getManager("com.h3c.imc.syslog");
  private String noItemMsg = SM.getString("syslog.datatable.no.records.tip");
  private SyslogTempletEntity commonTempEntity = new SyslogTempletEntity();
  private SyslogTempletParaEntity syslogTempletParaEntity = new SyslogTempletParaEntity();
  private SyslogCommonTempletMgr commonTempletMgr;
  private boolean en = Locale.getDefault().getLanguage().equalsIgnoreCase("en");
  private int tempContentLength = 512;
  private static Map<String, Boolean> startMap = new LinkedHashMap();
  private boolean showDiaplay = false;
  private boolean showParame = false;
  private boolean haveParame = false;
  private String checkObject = "content";
  Map parasPosition = new LinkedHashMap();
  
  static
  {
    startMap.put(SM.getString("sys.rule.add.select.items.stop"), Boolean.valueOf(false));
    startMap.put(SM.getString("sys.rule.add.select.items.start"), Boolean.valueOf(true));
  }
  
  public void init()
  {
    SyslogCommonTempListBean listBean = SyslogViewUtils.getSyslogCommonTempListBean();
    
    int winsMode = listBean.getWinsMode().intValue();
    if ((winsMode == 1) || (winsMode == 2))
    {
      if (commonTempEntity.getEnabled() == null) {
        showDiaplay = false;
      } else {
        showDiaplay = commonTempEntity.getEnabled().booleanValue();
      }
      if (commonTempEntity.getParams() == null) {
        haveParame = false;
      } else {
        haveParame = true;
      }
    }
    else
    {
      haveParame = false;
    }
  }
  
  public void onRegexTempCheck(ValueChangeEvent event)
  {
    commonTempEntity.setTempContent((String)event.getNewValue());
    if (commonTempEntity.getRegexMode().booleanValue())
    {
      String value = (String)event.getNewValue();
      parseTemepParame(value);
      FacesContext.getCurrentInstance().renderResponse();
    }
    else
    {
      haveParame = false;
    }
  }
  
  public void parseTemepParame(String tempContent)
  {
    commonTempEntity.setParams(null);
    int num = 0;
    Pattern pattern = null;
    try
    {
      pattern = Pattern.compile(tempContent);
    }
    catch (PatternSyntaxException e)
    {
      FacesContext fc = FacesContext.getCurrentInstance();
      FacesMessage msg = new FacesMessage(SM.getString("syslog.templet.error.regexNoAvail"));
      
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      fc.addMessage("mainForm:content", msg);
    }
    if (pattern != null)
    {
      String patternTemp1 = pattern.toString();
      String patternTemp2 = patternTemp1.replace("\\(", "");
      String patternTemp3 = patternTemp2.replace("\\)", "");
      num = calcuParenthesisNum(patternTemp3);
    }
    else
    {
      FacesContext fc = FacesContext.getCurrentInstance();
      FacesMessage msg = new FacesMessage(SM.getString("syslog.templet.error.contentWrong"));
      
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      fc.addMessage("mainForm:content", msg);
    }
    commonTempEntity.setParams(putValue(num));
  }
  
  public List<SyslogTempletParaEntity> putValue(int num)
  {
    List<SyslogTempletParaEntity> list = new ArrayList();
    if (num > 0)
    {
      haveParame = true;
      for (int i = 1; i <= num; i++)
      {
        SyslogTempletParaEntity para = new SyslogTempletParaEntity();
        para.setLocation(i);
        para.setParaName("");
        list.add(para);
      }
    }
    else
    {
      haveParame = false;
    }
    return list;
  }
  
  public void onRegexModeChange(ValueChangeEvent event)
  {
    Boolean newValue = (Boolean)event.getNewValue();
    commonTempEntity.setRegexMode(newValue);
    if (StringUtils.isBlank(commonTempEntity.getTempContent())) {
      return;
    }
    if (newValue.booleanValue())
    {
      parseTemepParame(commonTempEntity.getTempContent());
    }
    else
    {
      haveParame = false;
      commonTempEntity.setParams(null);
    }
    FacesContext.getCurrentInstance().renderResponse();
  }
  
  public String onConfirm()
  {
    SyslogCommonTempListBean listBean = SyslogViewUtils.getSyslogCommonTempListBean();
    
    int winsMode = listBean.getWinsMode().intValue();
    
    String msgOpt = winsMode == 1 ? "syslog.templet.opt.modifySuccessMsg" : "syslog.templet.opt.addSuccessMsg";
    
    String oplog = winsMode == 1 ? "syslog.templet.oplog.ModifyOpt" : "syslog.templet.oplog.addOpt";
    
    checkObject = "content";
    if (commonTempEntity.getRegexMode().booleanValue())
    {
      Pattern pattern = null;
      try
      {
        pattern = Pattern.compile(commonTempEntity.getTempContent());
      }
      catch (PatternSyntaxException e)
      {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage msg = new FacesMessage(SM.getString("syslog.templet.error.regexNoAvail"));
        
        msg.setSeverity(FacesMessage.SEVERITY_ERROR);
        fc.addMessage("mainForm:content", msg);
        return null;
      }
    }
    if (!checkContent(commonTempEntity.getTempContent())) {
      return null;
    }
    if (sameParas(commonTempEntity.getTempContent()))
    {
      FacesContext.getCurrentInstance().addMessage("mainForm:content", new FacesMessage(SM.getString("sys.upToTrap.addRule.sameparams")));
      
      return null;
    }
    if (commonTempEntity.getEnabled().booleanValue())
    {
      checkObject = "descriptionDisplay";
      if (!checkContent(commonTempEntity.getDisplayContent())) {
        return null;
      }
      List orinigalModule = new ArrayList();
      if (commonTempEntity.getRegexMode().booleanValue())
      {
        orinigalModule = getParamsName(commonTempEntity.getParams());
      }
      else
      {
        String filterSyslogModule = commonTempEntity.getTempContent();
        orinigalModule = getVarList(filterSyslogModule);
      }
      List displayModule = getVarList(commonTempEntity.getDisplayContent());
      
      StringBuffer sb = new StringBuffer();
      if (displayModule.size() != 0) {
        for (int i = 0; i < displayModule.size(); i++) {
          if (!orinigalModule.contains(displayModule.get(i))) {
            sb.append("$(" + displayModule.get(i) + "),");
          }
        }
      }
      String errMsg = sb.toString();
      if (!errMsg.isEmpty())
      {
        FacesContext.getCurrentInstance().addMessage("mainForm:descriptionDisplay", new FacesMessage(SM.getString("syslog.templet.oplog.addOpt.display", new Object[] { errMsg.substring(0, errMsg.length() - 1) })));
        
        return null;
      }
    }
    try
    {
      if (winsMode == 1)
      {
        commonTempletMgr.modifyTemplet(commonTempEntity);
        
        listBean.getOptResult().showSuccessMsgInOtherPage(SM.getString(msgOpt, new Object[] { commonTempEntity.getTempName() }));
        
        Operlog.insertSuccessLog(SM.getString(oplog, new Object[] { commonTempEntity.getTempName() }));
      }
      else
      {
        commonTempletMgr.addTemplet(commonTempEntity);
        
        listBean.getOptResult().showSuccessMsgInOtherPage(SM.getString(msgOpt, new Object[] { commonTempEntity.getTempName() }));
        
        Operlog.insertSuccessLog(SM.getString(oplog, new Object[] { commonTempEntity.getTempName() }));
      }
    }
    catch (SyslogException e)
    {
      switch (e.getErrorCode())
      {
      case 7: 
        FacesContext.getCurrentInstance().addMessage("mainForm:tempName", new FacesMessage(FacesMessage.SEVERITY_ERROR, SM.getString("syslog.templet.error.nameIsExist", new Object[] { commonTempEntity.getTempName() }), SM.getString("syslog.templet.error.nameIsExist", new Object[] { commonTempEntity.getTempName() })));
        
        Operlog.insertFailLog(SM.getString(oplog, new Object[] { commonTempEntity.getTempName() }), SM.getString("syslog.templet.error.nameIsExist", new Object[] { commonTempEntity.getTempName() }));
        
        return null;
      }
    }
    listBean.getOptResult().showFailureMsgInOtherPage(SM.getString("syslog.templet.error.tempNotExist", new Object[] { commonTempEntity.getTempName() }));
    
    Operlog.insertFailLog(SM.getString(oplog, new Object[] { commonTempEntity.getTempName() }), SM.getString("syslog.templet.error.tempNotExist", new Object[] { commonTempEntity.getTempName() }));
    break label861;
    Operlog.insertFailLog(SM.getString(oplog, new Object[] { commonTempEntity.getTempName() }), e.getErrorMsg());
    
    listBean.getOptResult().showFailureMsgInOtherPage(SM.getString("syslog.templet.opt.addFailureMsg", new Object[] { commonTempEntity.getTempName() }));
    label861:
    listBean.fireDataChanged();
    return "go_syslog_templetListPage";
  }
  
  public void onRadioChangeListener(ValueChangeEvent event)
  {
    Boolean flag = (Boolean)event.getNewValue();
    commonTempEntity.setEnabled(flag);
    showDiaplay = flag.booleanValue();
    FacesContext.getCurrentInstance().renderResponse();
  }
  
  public String onCancel()
  {
    SyslogCommonTempListBean listBean = SyslogViewUtils.getSyslogCommonTempListBean();
    
    listBean.fireDataChanged();
    return "go_syslog_templetListPage";
  }
  
  public static boolean isParenthesisNest(String str)
  {
    int n = str.indexOf("(");
    if (n >= 0)
    {
      String str2 = str.substring(n + 1);
      while (str2.length() > 0)
      {
        int leftParenthesis = str2.indexOf("(");
        int rightParenthesis = str2.indexOf(")");
        if ((leftParenthesis >= 0) && (leftParenthesis < rightParenthesis)) {
          return true;
        }
        if (leftParenthesis < 0) {
          return false;
        }
        boolean flag = isParenthesisNest(str2.substring(rightParenthesis + 1));
        if (!flag) {
          return false;
        }
      }
    }
    return false;
  }
  
  public int calcuParenthesisNum(String str)
  {
    parasPosition.clear();
    
    Stack stack = new Stack();
    for (int i = 0; i < str.length(); i++)
    {
      char everyChar = str.charAt(i);
      String temp = String.valueOf(everyChar);
      if (temp.equals("(")) {
        stack.push(Integer.valueOf(i));
      }
      if (temp.equals(")"))
      {
        if (stack.empty()) {
          return 0;
        }
        int topObjec = ((Integer)stack.pop()).intValue();
        if (stack.empty()) {
          parasPosition.put(Integer.valueOf(topObjec), Integer.valueOf(i));
        }
      }
    }
    return parasPosition.size();
  }
  
  private boolean checkContent(String content)
  {
    boolean check = true;
    
    Pattern pat1 = Pattern.compile("\\*+");
    
    Pattern pat2 = Pattern.compile("([^\\$]*(\\$\\(([^\\$\\(\\)\\{\\}\\*\\n\\r<>])+\\))[^\\$]*)+");
    
    Pattern pat3 = Pattern.compile(".*(\\$\\((\\s)*\\))+.*");
    Pattern pat4 = Pattern.compile("[^\\$]+");
    
    Pattern pat5 = Pattern.compile(".*(\\((\\$)+.*\\))+.*");
    Pattern pat6 = Pattern.compile(".*\\$\\(.*");
    Pattern pat7 = Pattern.compile(".*(\\((\\$\\()+.*\\))+.*");
    Pattern pat8 = Pattern.compile(".*\\$.*");
    if ((pat4.matcher(content).matches()) && (!pat1.matcher(content).matches()))
    {
      check = true;
      return check;
    }
    String[] tmpTeam1 = content.split("\\$\\(");
    String[] tmpTeam2 = content.split("\\$");
    if (tmpTeam1.length != tmpTeam2.length)
    {
      check = false;
      FacesContext.getCurrentInstance().addMessage("mainForm:" + checkObject, new FacesMessage(SM.getString("syslog.templet.list.templet.content.error")));
      
      return check;
    }
    if ((pat1.matcher(content).matches()) || ((pat8.matcher(content).matches()) && (!pat6.matcher(content).matches())) || ((!pat7.matcher(content).matches()) && (pat5.matcher(content).matches())) || (!pat6.matcher(content).matches()) || (!pat2.matcher(content).matches()) || (pat3.matcher(content).matches()))
    {
      check = false;
      FacesContext.getCurrentInstance().addMessage("mainForm:" + checkObject, new FacesMessage(SM.getString("syslog.templet.list.templet.content.error")));
      
      return check;
    }
    if (!content.matches("([^\\$]*(\\$\\()){1}[^\\$]*"))
    {
      String[] tmp = content.split("\\$\\(");
      for (int i = 0; i < tmp.length - 1; i++)
      {
        Pattern pat = Pattern.compile("(\\S)+\\){1}[\\s\\*]*");
        if (pat.matcher(tmp[i]).matches())
        {
          check = false;
          FacesContext.getCurrentInstance().addMessage("mainForm:" + checkObject, new FacesMessage(SM.getString("syslog.templet.list.templet.content.error")));
          
          return check;
        }
      }
    }
    return check;
  }
  
  public boolean sameParas(String template)
  {
    List<RuleParam> params = new ArrayList();
    
    char[] a = template.toCharArray();
    
    char[] tmp = new char[a.length];
    
    int j = 0;
    for (int i = 0; i < a.length; i++) {
      if (a[i] == '$')
      {
        if ((i + 1 < a.length) && (a[(i + 1)] == '(')) {
          for (j = 0; j + i + 2 < a.length; j++)
          {
            if (a[(j + i + 2)] == ')') {
              break;
            }
            tmp[j] = a[(j + i + 2)];
          }
        }
        if (j != 0)
        {
          char[] temp = new char[j];
          for (int k = 0; k < j; k++) {
            temp[k] = tmp[k];
          }
          String paraName = String.valueOf(temp);
          tmp = new char[a.length];
          RuleParam param = new RuleParam();
          param.setParaName(paraName.trim());
          j = 0;
          if (!params.contains(param)) {
            params.add(param);
          } else {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  public List getParamsName(List<SyslogTempletParaEntity> list)
  {
    List varList = new ArrayList();
    if ((list != null) && 
      (list.size() > 0)) {
      for (SyslogTempletParaEntity sEntity : list) {
        varList.add(sEntity.getParaName());
      }
    }
    return varList;
  }
  
  public List getVarList(String module)
  {
    String[] str = module.split("\\$\\(");
    
    List varList = new ArrayList();
    for (int i = 1; i < str.length; i++)
    {
      String[] vars = str[i].split("\\)");
      varList.add(vars[0]);
    }
    return varList;
  }
  
  public void setCommonTempletMgr(SyslogCommonTempletMgr commonTempletMgr)
  {
    this.commonTempletMgr = commonTempletMgr;
  }
  
  public SyslogTempletEntity getCommonTempEntity()
  {
    return commonTempEntity;
  }
  
  public void setCommonTempEntity(SyslogTempletEntity commonTempEntity)
  {
    this.commonTempEntity = commonTempEntity;
  }
  
  public int getTempContentLength()
  {
    if (en) {
      tempContentLength = 512;
    } else {
      tempContentLength = 256;
    }
    return tempContentLength;
  }
  
  public Map<String, Boolean> getStartMap()
  {
    return startMap;
  }
  
  public boolean isShowDiaplay()
  {
    return showDiaplay;
  }
  
  public void setShowDiaplay(boolean showDiaplay)
  {
    this.showDiaplay = showDiaplay;
  }
  
  public SyslogTempletParaEntity getSyslogTempletParaEntity()
  {
    return syslogTempletParaEntity;
  }
  
  public void setSyslogTempletParaEntity(SyslogTempletParaEntity syslogTempletParaEntity)
  {
    this.syslogTempletParaEntity = syslogTempletParaEntity;
  }
  
  public boolean isShowParame()
  {
    return showParame;
  }
  
  public void setShowParame(boolean showParame)
  {
    this.showParame = showParame;
  }
  
  public boolean isHaveParame()
  {
    return haveParame;
  }
  
  public void setHaveParame(boolean haveParame)
  {
    this.haveParame = haveParame;
  }
  
  public String getNoItemMsg()
  {
    return noItemMsg;
  }
  
  public void setNoItemMsg(String noItemMsg)
  {
    this.noItemMsg = noItemMsg;
  }
}

/* Location:
 * Qualified Name:     com.h3c.imc.syslog.templet.view.SyslogCommonTempConfigBean
 * Java Class Version: 7 (51.0)
 * JD-Core Version:    0.7.1
 */