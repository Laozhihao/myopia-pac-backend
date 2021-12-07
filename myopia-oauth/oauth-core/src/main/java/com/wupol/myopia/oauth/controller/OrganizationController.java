package com.wupol.myopia.oauth.controller;


import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.oauth.domain.model.Organization;
import com.wupol.myopia.oauth.service.OrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author wulizhou
 * @Date 2021-12-06
 */
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/oauth/organization")
public class OrganizationController {

    @Autowired
    private OrganizationService organizationService;

    @PutMapping
    public Organization updateOrganization(@RequestBody @Valid Organization organization) {
        organizationService.update(organization, new Organization().setOrgId(organization.getOrgId()).setSystemCode(organization.getSystemCode()).setUserType(organization.getUserType()));
        return organizationService.get(organization.getOrgId(), organization.getSystemCode(), organization.getUserType());
    }

    @PostMapping
    public Organization addOrganization(@RequestBody @Valid Organization organization) {
        organizationService.save(organization);
        return organizationService.get(organization.getOrgId(), organization.getSystemCode(), organization.getUserType());
    }

}
