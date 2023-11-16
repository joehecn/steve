/*
 * SteVe - SteckdosenVerwaltung - https://github.com/steve-community/steve
 * Copyright (C) 2013-2023 SteVe Community Team
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.web.controller;

import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import de.rwth.idsg.steve.service.ChargePointService12_Client;
import de.rwth.idsg.steve.service.ChargePointService15_Client;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.web.dto.ocpp.ChangeConfigurationParams;
import de.rwth.idsg.steve.web.dto.ocpp.ClearChargingProfileParams;
import de.rwth.idsg.steve.web.dto.ocpp.ConfigurationKeyEnum;
import de.rwth.idsg.steve.web.dto.ocpp.ConfigurationKeyReadWriteEnum;
import de.rwth.idsg.steve.web.dto.ocpp.GetCompositeScheduleParams;
import de.rwth.idsg.steve.web.dto.ocpp.GetConfigurationParams;
import de.rwth.idsg.steve.web.dto.ocpp.SetChargingProfileParams;
import de.rwth.idsg.steve.web.dto.ocpp.TriggerMessageParams;
import ocpp.cs._2015._10.RegistrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.rwth.idsg.steve.web.dto.ocpp.ConfigurationKeyReadWriteEnum.R;
import static de.rwth.idsg.steve.web.dto.ocpp.ConfigurationKeyReadWriteEnum.RW;

import org.springframework.web.bind.annotation.RequestHeader;
import de.rwth.idsg.steve.utils.PropertiesFileLoader;
import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStartTransactionParams;
import de.rwth.idsg.steve.web.dto.ocpp.RemoteStopTransactionParams;
import de.rwth.idsg.steve.service.WebhookMessage;
import com.google.gson.Gson;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 15.03.2018
 */
@Controller
@RequestMapping(value = "/manager/operations/v1.6")
public class Ocpp16Controller extends Ocpp15Controller {

    @Autowired
    @Qualifier("ChargePointService16_Client")
    private ChargePointService16_Client client16;

    @Autowired private ChargingProfileRepository chargingProfileRepository;

    // -------------------------------------------------------------------------
    // Paths
    // -------------------------------------------------------------------------

    private static final String GET_COMPOSITE_PATH = "/GetCompositeSchedule";
    private static final String CLEAR_CHARGING_PATH = "/ClearChargingProfile";
    private static final String SET_CHARGING_PATH = "/SetChargingProfile";
    private static final String TRIGGER_MESSAGE_PATH = "/TriggerMessage";
    private static final String REMOTE_START_TX_PATH = "/RemoteStartTransaction";
    private static final String REMOTE_STOP_TX_PATH = "/RemoteStopTransaction";
    private static final String INTERNAL_REMOTE_START_TX_PATH = "/internal/RemoteStartTransaction";
    private static final String INTERNAL_REMOTE_STOP_TX_PATH = "/internal/RemoteStopTransaction";
    private static final String INTERNAL_REDIRECT_RESPONSE_PATH = "redirect:/internal/response/";
    private static final String INTERNAL_REDIRECT_TASKS_PATH = "redirect:/manager/operations/tasks/internal/";

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    protected ChargePointService16_Client getClient16() {
        return client16;
    }

    @Override
    protected ChargePointService15_Client getClient15() {
        return client16;
    }

    @Override
    protected ChargePointService12_Client getClient12() {
        return client16;
    }

    @Override
    protected void setCommonAttributesForTx(Model model) {
        model.addAttribute("cpList", chargePointHelperService.getChargePoints(OcppVersion.V_16));
        model.addAttribute("opVersion", "v1.6");
    }

    /**
     * From OCPP 1.6 spec: "While in pending state, the following Central
     * System initiated messages are not allowed: RemoteStartTransaction.req
     * and RemoteStopTransaction.req"
     *
     * Conversely, it means all other operations are allowed for pending state.
     */
    @Override
    protected void setCommonAttributes(Model model) {
        List<RegistrationStatus> inStatusFilter = Arrays.asList(RegistrationStatus.ACCEPTED, RegistrationStatus.PENDING);
        model.addAttribute("cpList", chargePointHelperService.getChargePoints(OcppVersion.V_16, inStatusFilter));
        model.addAttribute("opVersion", "v1.6");
    }

    @Override
    protected Map<String, String> getConfigurationKeys(ConfigurationKeyReadWriteEnum confEnum) {
        switch (confEnum) {
            case R:
                return ConfigurationKeyEnum.OCPP_16_MAP_R;
            case RW:
                return ConfigurationKeyEnum.OCPP_16_MAP_RW;
            default:
                return Collections.emptyMap();
        }
    }

    @Override
    protected String getRedirectPath() {
        return "redirect:/manager/operations/v1.6/ChangeAvailability";
    }

    @Override
    protected String getPrefix() {
        return "op16";
    }

    // -------------------------------------------------------------------------
    // Old Http methods with changed logic
    // -------------------------------------------------------------------------

    @RequestMapping(value = GET_CONF_PATH, method = RequestMethod.GET)
    public String getGetConf(Model model) {
        setCommonAttributes(model);
        model.addAttribute(PARAMS, new GetConfigurationParams());
        model.addAttribute("ocppConfKeys", getConfigurationKeys(R));
        return getPrefix() + GET_CONF_PATH;
    }

    @RequestMapping(value = CHANGE_CONF_PATH, method = RequestMethod.GET)
    public String getChangeConf(Model model) {
        setCommonAttributes(model);
        model.addAttribute(PARAMS, new ChangeConfigurationParams());
        model.addAttribute("ocppConfKeys", getConfigurationKeys(RW));
        return getPrefix() + CHANGE_CONF_PATH;
    }

    @RequestMapping(value = GET_CONF_PATH, method = RequestMethod.POST)
    public String postGetConf(@Valid @ModelAttribute(PARAMS) GetConfigurationParams params,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            setCommonAttributes(model);
            model.addAttribute("ocppConfKeys", getConfigurationKeys(R));
            return getPrefix() + GET_CONF_PATH;
        }
        return REDIRECT_TASKS_PATH + getClient15().getConfiguration(params);
    }

    // -------------------------------------------------------------------------
    // New Http methods (GET)
    // -------------------------------------------------------------------------

    @RequestMapping(value = GET_COMPOSITE_PATH, method = RequestMethod.GET)
    public String getGetCompositeSchedule(Model model) {
        setCommonAttributes(model);
        model.addAttribute(PARAMS, new GetCompositeScheduleParams());
        return getPrefix() + GET_COMPOSITE_PATH;
    }

    @RequestMapping(value = CLEAR_CHARGING_PATH, method = RequestMethod.GET)
    public String getClearChargingProfile(Model model) {
        setCommonAttributes(model);
        model.addAttribute("profileList", chargingProfileRepository.getBasicInfo());
        model.addAttribute(PARAMS, new ClearChargingProfileParams());
        return getPrefix() + CLEAR_CHARGING_PATH;
    }

    @RequestMapping(value = SET_CHARGING_PATH, method = RequestMethod.GET)
    public String getSetChargingProfile(Model model) {
        setCommonAttributes(model);
        model.addAttribute("profileList", chargingProfileRepository.getBasicInfo());
        model.addAttribute(PARAMS, new SetChargingProfileParams());
        return getPrefix() + SET_CHARGING_PATH;
    }

    @RequestMapping(value = TRIGGER_MESSAGE_PATH, method = RequestMethod.GET)
    public String getTriggerMessage(Model model) {
        setCommonAttributes(model);
        model.addAttribute(PARAMS, new TriggerMessageParams());
        return getPrefix() + TRIGGER_MESSAGE_PATH;
    }

    // -------------------------------------------------------------------------
    // Http methods (POST)
    // -------------------------------------------------------------------------

    @RequestMapping(value = TRIGGER_MESSAGE_PATH, method = RequestMethod.POST)
    public String postTriggerMessage(@Valid @ModelAttribute(PARAMS) TriggerMessageParams params,
                                     BindingResult result, Model model) {
        if (result.hasErrors()) {
            setCommonAttributes(model);
            return getPrefix() + TRIGGER_MESSAGE_PATH;
        }
        return REDIRECT_TASKS_PATH + getClient16().triggerMessage(params);
    }

    @RequestMapping(value = SET_CHARGING_PATH, method = RequestMethod.POST)
    public String postSetChargingProfile(@Valid @ModelAttribute(PARAMS) SetChargingProfileParams params,
                                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            setCommonAttributes(model);
            return getPrefix() + SET_CHARGING_PATH;
        }
        return REDIRECT_TASKS_PATH + getClient16().setChargingProfile(params);
    }

    @RequestMapping(value = CLEAR_CHARGING_PATH, method = RequestMethod.POST)
    public String postClearChargingProfile(@Valid @ModelAttribute(PARAMS) ClearChargingProfileParams params,
                                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            setCommonAttributes(model);
            return getPrefix() + CLEAR_CHARGING_PATH;
        }
        return REDIRECT_TASKS_PATH + getClient16().clearChargingProfile(params);
    }

    @RequestMapping(value = GET_COMPOSITE_PATH, method = RequestMethod.POST)
    public String postGetCompositeSchedule(@Valid @ModelAttribute(PARAMS) GetCompositeScheduleParams params,
                                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            setCommonAttributes(model);
            return getPrefix() + GET_COMPOSITE_PATH;
        }
        return REDIRECT_TASKS_PATH + getClient16().getCompositeSchedule(params);
    }

    private Boolean webApiCheck(Map<String, String> headers) {
        PropertiesFileLoader p = new PropertiesFileLoader("main.properties");
        String API_KEY = p.getOptionalString("webapi.key");
        String API_VALUE = p.getOptionalString("webapi.value");

        String api_value = headers.get(API_KEY);
        return API_VALUE.equals(api_value);
    }

    private Integer securityCheck(String body, Map<String, String> headers) {
        try {
            Long curTime = System.currentTimeMillis();
            Long time = Long.parseLong(headers.get("t"));
            if (curTime - time > 3000) {
                System.out.printf("Expired request time: %d\n", time);
                return 400;
            }

            String signature = headers.get("signature");
            String sig = WebhookMessage.getSignature(body, time);
            if (sig.equals(signature)) {
                return 200;
            } else {
                System.out.println("Form data has been modified");
                return 403;
            }
        } catch (Exception e) {
            System.out.println(e);
            return 500;
        }
    }

    @RequestMapping(value = INTERNAL_REMOTE_START_TX_PATH, method = RequestMethod.POST)
    public String internalPostRemoteStartTx(@Valid @ModelAttribute(PARAMS) RemoteStartTransactionParams params,
                                    BindingResult result, Model model, @RequestHeader Map<String, String> headers) {
        if (!this.webApiCheck(headers)) {
            System.out.println("API Key or API Value is not matched");
            return INTERNAL_REDIRECT_RESPONSE_PATH + 403;
        }
        /**
         * The body is a JSON string, and the format must be:
         * {
         *     "connectorId": 1,
         *     "idTag": "idTag",
         *     "chargePointSelectList": [
         *         {
         *             "ocppTransport": "JSON",
         *             "chargeBoxId": "boxId",
         *             "endpointAddress": "-"
         *         }
         *     ]
         * }
         * 
         * Note:
         * - If the connectorId is greater the 0, then the first key must be "connectorId".
         *   Otherwise, the first key is "idTag".
         */
        Gson gson = new Gson();
        String body = gson.toJson(params);
        Integer status = this.securityCheck(body, headers);
        if (status != 200) {
            return INTERNAL_REDIRECT_RESPONSE_PATH + status;
        }
        if (result.hasErrors()) {
            setCommonAttributesForTx(model);
            setActiveUserIdTagList(model);
            return getPrefix() + REMOTE_START_TX_PATH;
        }
        int taskId = getClient16().remoteStartTransaction(params);
        return INTERNAL_REDIRECT_TASKS_PATH + taskId;
    }

    @RequestMapping(value = INTERNAL_REMOTE_STOP_TX_PATH, method = RequestMethod.POST)
    public String internalRemoteStopTx(@Valid @ModelAttribute(PARAMS) RemoteStopTransactionParams params,
                                   BindingResult result, Model model, @RequestHeader Map<String, String> headers) {
        if (!this.webApiCheck(headers)) {
            System.out.println("API Key or API Value is not matched");
            return INTERNAL_REDIRECT_RESPONSE_PATH + 403;
        }
        /**
         * The body is a JSON string, and the format must be:
         * {
         *     "transactionId": 1,
         *     "chargePointSelectList": [
         *         {
         *             "ocppTransport": "JSON",
         *             "chargeBoxId": "boxId",
         *             "endpointAddress": "-"
         *         }
         *     ]
         * }
         */
        Gson gson = new Gson();
        String body = gson.toJson(params);
        Integer status = this.securityCheck(body, headers);
        if (status != 200) {
            return INTERNAL_REDIRECT_RESPONSE_PATH + status;
        }
        if (result.hasErrors()) {
            setCommonAttributesForTx(model);
            return getPrefix() + REMOTE_STOP_TX_PATH;
        }
        int taskId = getClient16().remoteStopTransaction(params);
        return INTERNAL_REDIRECT_TASKS_PATH + taskId;
    }
}
