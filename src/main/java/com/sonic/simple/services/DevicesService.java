package com.sonic.simple.services;

import com.alibaba.fastjson.JSONObject;
import com.sonic.simple.models.Devices;
import com.sonic.simple.models.http.DevicePwdChange;
import com.sonic.simple.models.http.UpdateDeviceImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author ZhouYiXun
 * @des 设备逻辑层
 * @date 2021/8/16 22:51
 */
public interface DevicesService {
    boolean savePwd(DevicePwdChange devicePwdChange);

    void updateImg(UpdateDeviceImg updateDeviceImg);

    void save(Devices devices);

    Page<Devices> findAll(List<String> iOSVersion, List<String> androidVersion, List<String> manufacturer,
                          List<String> cpu, List<String> size, List<Integer> agentId, List<String> status,
                          String deviceInfo, Pageable pageable);

    List<Devices> findAll(int platform);

    List<Devices> findByIdIn(List<Integer> ids);

    Devices findByAgentIdAndUdId(int agentId, String udId);

    JSONObject getFilterOption();

    void deviceStatus(JSONObject jsonObject);

    Devices findById(int id);
}
