package egov.dataupload.utils;

import egov.dataupload.config.Configuration;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.*;
import org.egov.mdms.service.MdmsClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class TenantService {

    @Autowired
    private Configuration configuration;
    @Autowired
    private RestTemplate restTemplate;

    private String moduleName = "tenant";
    private String masterDetailsName = "citymodule";
    private String filter = "$.[?(@.module=='CASE_MANAGEMENT')].tenants.*";

    public List<String> getAllTenantIds() {

        String rootTenantId = configuration.getRootTenantId();

        MasterDetail masterDetail = MasterDetail.builder().name(masterDetailsName).filter(filter).build();
        ModuleDetail moduleDetail =
                ModuleDetail.builder().moduleName(moduleName).masterDetails(Collections.singletonList(masterDetail)).build();
        MdmsCriteria mdmsCriteria =
                MdmsCriteria.builder().tenantId(rootTenantId).moduleDetails(Collections.singletonList(moduleDetail)).build();

        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(RequestInfo.builder().build()).build();

        MdmsResponse mdmsResponse = restTemplate.postForObject(configuration.getMdmsHost() + configuration.getMdmsEndPoint(),
                mdmsCriteriaReq, MdmsResponse.class);

        Map<String, Map<String, JSONArray>> mdmsRes = mdmsResponse.getMdmsRes();

        JSONArray mdmsResValues = mdmsRes.get(moduleName).get(masterDetailsName);

        List<String> tenantIds = new ArrayList<>();

        for (Object mdmsResValue : mdmsResValues) {
            HashMap mdmsValue = (HashMap) mdmsResValue;
            String tenant = mdmsValue.get("code").toString();
            tenantIds.add(tenant);
        }

        return tenantIds;
    }


}
