package com.didichuxing.datachannel.arius.admin.core.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        return null;
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
