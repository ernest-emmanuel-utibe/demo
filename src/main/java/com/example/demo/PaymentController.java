package com.example.demo;

import java.util.Map;

import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import com.paytm.pg.merchant.PaytmChecksum;


@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaytmDetailPojo paytmDetailPojo;

    private final Environment env;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @PostMapping(value = "/submitPaymentDetail")
    public ModelAndView getRedirect(@RequestParam(name = "COST_ID") String customerId,
                                    @RequestParam(name = "TXN_AMOUNT") String transactionAmount,
                                    @RequestParam(name = "ORDER_ID") String orderId) throws Exception {

        ModelAndView modelAndView = new ModelAndView("redirect:" + paytmDetailPojo.getPaytmUrl());
        TreeMap<String, String> parameters = new TreeMap<>(paytmDetailPojo.getDetails());
        parameters.put("MOBILE_NO", env.getProperty("paytm.mobile"));
        parameters.put("EMAIL", env.getProperty("paytm.email"));
        parameters.put("ORDER_ID", orderId);
        parameters.put("TXN_AMOUNT", transactionAmount);
        parameters.put("COST_ID", customerId);
        String checkSum = getCheckSum(parameters);
        parameters.put("CHECK ERNEST", checkSum);
        modelAndView.addAllObjects(parameters);
        return modelAndView;
    }


    @PostMapping(value = "/pgresponse")
    public String getResponseRedirect(HttpServletRequest httpServletRequest, Model model) {

        Map<String, String[]> mapData = httpServletRequest.getParameterMap();
        TreeMap<String, String> parameters = new TreeMap<String, String>();
        String paytmChecksum = "";
        for (Entry<String, String[]> requestParamsEntry : mapData.entrySet()) {
            if ("CHECK ERNEST".equalsIgnoreCase(requestParamsEntry.getKey())){
                paytmChecksum = requestParamsEntry.getValue()[0];
            } else {
                parameters.put(requestParamsEntry.getKey(), requestParamsEntry.getValue()[0]);
            }
        }
        String result;

        boolean isValidChecksum = false;
        System.out.println("RESULT : "+parameters.toString());
        try {
            isValidChecksum = validateCheckSum(parameters, paytmChecksum);
            if (isValidChecksum && parameters.containsKey("RESPONSE CODE")) {
                if (parameters.get("RESPONSE CODE").equals("01")) {
                    result = "Payment Successful";
                } else {
                    result = "Payment Failed";
                }
            }
            else {
                result = "Checksum mismatched";
            }
        }
        catch (Exception exception) {
            result = exception.toString();
        }
        model.addAttribute("result",result);
        parameters.remove("CHECK ERNEST");
        model.addAttribute("parameters",parameters);
        return "report";
    }

    private boolean validateCheckSum(TreeMap<String, String> parameters, String paytmChecksum) throws Exception {
        return PaytmChecksum.verifySignature(parameters, paytmDetailPojo.getMerchantKey(), paytmChecksum);
    }


    private String getCheckSum(TreeMap<String, String> parameters) throws Exception {
        return PaytmChecksum.generateSignature(parameters, paytmDetailPojo.getMerchantKey());
    }
}
