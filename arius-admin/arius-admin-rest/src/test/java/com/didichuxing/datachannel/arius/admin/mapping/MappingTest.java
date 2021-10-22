package com.didichuxing.datachannel.arius.admin.mapping;

import java.util.ArrayList;
import java.util.List;

import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplatePhyMappingManager;
import com.didichuxing.datachannel.arius.admin.persistence.es.cluster.ESIndexDAO;
import com.didichuxing.datachannel.arius.elasticsearch.client.response.setting.common.MappingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.client.mapping.Field;
import com.didichuxing.datachannel.arius.admin.common.bean.entity.template.IndexTemplateLogicWithMapping;
import com.didichuxing.datachannel.arius.admin.client.bean.common.Result;
import com.didichuxing.datachannel.arius.admin.biz.template.srv.mapping.TemplateLogicMappingManager;
import com.didichuxing.datachannel.arius.admin.AriusAdminApplicationTests;

/**
 * @author d06679
 * @date 2019/4/11
 */
public class MappingTest extends AriusAdminApplicationTests {

    @Autowired
    private TemplatePhyMappingManager templatePhyMappingManager;


    @Autowired
    private TemplateLogicMappingManager templateLogicMappingManager;

    @Autowired
    private ESIndexDAO esIndexDAO;

    @Test
    public void testSyncMapping() {
        Result result = templatePhyMappingManager.syncMappingConfig("Arius公共集群(6.5.1)", "mysql_market_order", "mysql_market_order*", "_yyyy-MM-dd");
        //Result result = templatePhyMappingManager.syncMappingConfig("es-test-7", "cn_test_mapping", "cn_test_mapping*", "_yyyy-MM-dd");
        MappingConfig mappingConfig = (MappingConfig) result.getData();

        System.out.println(mappingConfig.toJson().toJSONString());
    }

    @Test
    public void testSetSource() {
        String cluster = "es-test-7";//"ecm-offline-test";
        String templateName = "cn_test_mapping";//"zhonghua_test";
        Result<MappingConfig> mappingConfigResult = templatePhyMappingManager.getMapping(cluster, templateName);
        if (mappingConfigResult.failed()) {
            System.out.println(mappingConfigResult.getMessage());
            return;
        }
        MappingConfig mappingConfig = (MappingConfig) mappingConfigResult.getData();
        mappingConfig.disableSource();
        Result result = templatePhyMappingManager.updateMapping(cluster, templateName, mappingConfig.toJson().toJSONString());
        System.out.println(result);
    }

    @Test
    public void logicCheck() throws Exception {

        Result<IndexTemplateLogicWithMapping> r = templateLogicMappingManager.getTemplateWithMapping(7070);
        List<Field> fields = r.getData().getFields();

        Field field = fields.get(0);
        field.setName("a");
        field.setType("date");
        field.setIndexType(2);
        field.setSortType(1);
        field.setAnalyzerType(null);

        field = new Field();
        field.setName("helloworld");
        field.setType("date");
        field.setIndexType(2);
        field.setSortType(1);
        field.setAnalyzerType(null);
        fields.add(field);


        Result result = templateLogicMappingManager.checkFields(7070, fields);
        if(result.failed()) {
            throw new Exception(result.getMessage());
        }

        System.out.println("hello world");
    }

    @Test
    public void logicList() throws Exception {
        Result<IndexTemplateLogicWithMapping> r = templateLogicMappingManager.getTemplateWithMapping(7160);


        templateLogicMappingManager.updateProperties(7160, r.getData().getTypeProperties());
//        Result<IndexTemplateLogicWithMapping> r = templateLogicMappingManager.getTemplateWithMapping(7322);
        List<Field> fields = r.getData().getFields();

        System.out.println(JSON.toJSONString(fields));
    }

    @Test
    public void logicCheckForNew() throws Exception {
        List<Field> fields = new ArrayList<>();

        Field field;

        field = new Field();
        field.setName("a");
        field.setType("integer");
        field.setIndexType(2);
        field.setSortType(1);
        fields.add(field);

        field = new Field();
        field.setName("b");
        field.setType("string");
        field.setIndexType(3);
        field.setAnalyzerType(2);
        fields.add(field);

        field = new Field();
        field.setName("c");
        field.setType("date");
        field.setIndexType(2);
        field.setSortType(1);
        fields.add(field);
    }

    @Test
    public void physicalCheck() throws Exception {


        String mapping = "{\n" +
                "  \"xxx\": {\n" +
                "    \"properties\": {\n" +
                "      \"refundFee\": {\n" +
                "        \"type\": \"long\"\n" +
                "      }\n" +
                "      }\n" +
                "      },\n" +
                "  \"refund\": {\n" +
                "    \"properties\": {\n" +
                "      \"refundFee\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"orderId\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"refundReason\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"refundStatus\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"updateTime\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"version\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"refundExtra\": {\n" +
                "        \"properties\": {\n" +
                "          \"_refunded_time\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"appId\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"supplierName\": {\n" +
                "            \"type\": \"text\"\n" +
                "          },\n" +
                "          \"companyStoreName\": {\n" +
                "            \"type\": \"text\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"outTradeId\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"isRefundAll\": {\n" +
                "        \"type\": \"boolean\"\n" +
                "      },\n" +
                "      \"auditTime\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"createTime\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"refundSource\": {\n" +
                "        \"type\": \"long\"\n" +
                "      },\n" +
                "      \"auditComment\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"id\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"payId\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"refundFeeDetail\": {\n" +
                "        \"properties\": {\n" +
                "          \"refund_fee\": {\n" +
                "            \"type\": \"double\"\n" +
                "          },\n" +
                "          \"channel_id\": {\n" +
                "            \"type\": \"double\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"customer\": {\n" +
                "        \"properties\": {\n" +
                "          \"bigUid\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"totalId\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"duid\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"role\": {\n" +
                "            \"type\": \"long\"\n" +
                "          },\n" +
                "          \"cellPone\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"extra\": {\n" +
                "            \"properties\": {\n" +
                "              \"plateNo\": {\n" +
                "                \"type\": \"keyword\"\n" +
                "              },\n" +
                "              \"consumerPhone\": {\n" +
                "                \"type\": \"keyword\"\n" +
                "              },\n" +
                "              \"shippingAddress\": {\n" +
                "                \"properties\": {\n" +
                "                  \"area\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"duid\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"city\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"receiverName\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"telephone\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"updateTime\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"cityId\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"provinceId\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"areaId\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"province\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"createTime\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"street\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"detail\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"id\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"defaultAdr\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"passportId\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  },\n" +
                "                  \"streetId\": {\n" +
                "                    \"type\": \"keyword\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"consumerName\": {\n" +
                "                \"type\": \"keyword\"\n" +
                "              }\n" +
                "            }\n" +
                "          },\n" +
                "          \"userName\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"cellPhone\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          },\n" +
                "          \"passportId\": {\n" +
                "            \"type\": \"keyword\"\n" +
//                "            \"type\": \"string\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";

    }
}
