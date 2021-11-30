package com.didichuxing.datachannel.arius.admin.core.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.didichuxing.datachannel.arius.admin.common.constant.AriusConfigConstant;
import com.didichuxing.datachannel.arius.admin.core.service.common.AriusConfigInfoService;
import com.didiglobal.logi.log.ILog;
import com.didiglobal.logi.log.LogFactory;
import com.google.common.collect.Lists;

/**
 * 发送邮件工具
 *
 * @author d06679
 * @date 2017/12/6
 */
@Component
public class MailTool {

    private static final ILog      LOGGER = LogFactory.getLog(MailTool.class);

    private static final String    ARIUS  = "arius_admin@didichuxing.com";

    @Autowired
    private AriusConfigInfoService ariusConfigInfoService;

    public boolean sendMailContentToArius(MailContent content) {
        return send(content.getSubject(), content.getHtmlMsg(), Lists.newArrayList(ARIUS), Lists.newArrayList(ARIUS));
    }

    /**
     * 发送HTML格式的email
     *
     * @param subject 邮件主题
     * @param htmlMsg html格式的邮件正文
     * @param toAddr 收件人列表
     * @param ccAddr 抄送列表
     */
    public boolean send(String subject, String htmlMsg, List<String> toAddr, List<String> ccAddr) {
        try {
            HtmlEmail email = constructEmail(subject, htmlMsg, replaceT2Leader(toAddr), replaceT2Leader(ccAddr));
            String result = email.send();
            LOGGER.info("class=MailTool||method=send||email send||content={}||result={}", htmlMsg, result);
        } catch (EmailException e) {
            LOGGER.warn("class=MailTool||method=send||email send got an exception,msg={}", e.getMessage());
            return false;
        }

        return true;
    }

    private List<String> replaceT2Leader(List<String> addrs) {
        Set<String> t2LeaderMails = ariusConfigInfoService
            .stringSettingSplit2Set(AriusConfigConstant.ARIUS_COMMON_GROUP, "arius.didi.t2.leader.mail", "", ",");

        List<String> newAddrs = new ArrayList<>();
        for (String addr : addrs) {
            if (t2LeaderMails.contains(addr)) {
                addr = ARIUS;
            }

            newAddrs.add(addr);
        }

        return newAddrs;
    }

    private HtmlEmail constructEmail(String subject, String htmlMsg, List<String> toAddr,
                                     List<String> ccAddr) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        // 设置发送主机的服务器地址
        email.setTLS(true);
        email.setHostName("mail.didichuxing.com");
        //内网机器端口25
        email.setSmtpPort(587);

        // 发件人邮箱
        email.setFrom(ARIUS, "Arius服务中心");
        email.setAuthentication(ARIUS, "N0iMhUsJ");
        email.setCharset("utf-8");
        email.setSubject(subject);
        email.setHtmlMsg(htmlMsg);

        List<InternetAddress> toAddrList = Lists.newArrayList();
        for (String emailAddress : toAddr) {
            addInternetAddress(toAddrList, emailAddress);
        }
        email.setTo(toAddrList);

        try {
            List<InternetAddress> ccAddrList = Lists.newArrayList();
            if (CollectionUtils.isNotEmpty(ccAddr)) {
                for (String emailAddress : ccAddr) {
                    addInternetAddress(ccAddrList, emailAddress);
                }
            }
            ccAddrList.add(new InternetAddress(ARIUS));
            email.setCc(ccAddrList);
        } catch (AddressException e) {
            LOGGER.warn("class=MailTool||method=constructEmail||msg=exception:{}", e.getMessage());
        }

        return email;
    }

    private void addInternetAddress(List<InternetAddress> ccAddrList, String emailAddress) {
        try {
            InternetAddress internetAddress = new InternetAddress(emailAddress.trim());
            ccAddrList.add(internetAddress);
        } catch (AddressException e) {
            LOGGER.warn("class=MailTool||method=constructEmail||msg=illegal email address:" + emailAddress, e);
        }
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static String readMailHtmlFileInJarFile(String fileName) {
        InputStream inputStream = MailTool.class.getClassLoader().getResourceAsStream("html/" + fileName);
        if (inputStream != null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            try {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            } catch (IOException e) {
                LOGGER.error("class=MailTool||method=readMailHtmlFileInJarFile||errMsg=read file {} error. ", fileName,
                    e);
                return "";
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error(
                        "class=MailTool||method=readMailHtmlFileInJarFile||errMsg=fail to close file {} error. ",
                        fileName, e);
                }
            }
        } else {
            LOGGER.error(
                "class=MailTool||class=MailTool||method=readMailHtmlFileInJarFile||errMsg=fail to read file {} content",
                fileName);
            return "";
        }
    }

}
