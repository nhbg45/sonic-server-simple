package com.sonic.simple.services.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sonic.simple.dao.ResultDetailRepository;
import com.sonic.simple.dao.ResultsRepository;
import com.sonic.simple.models.*;
import com.sonic.simple.models.interfaces.ResultDetailStatus;
import com.sonic.simple.models.interfaces.ResultStatus;
import com.sonic.simple.services.*;
import com.sonic.simple.tools.RobotMsgTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author ZhouYiXun
 * @des 测试结果逻辑实现
 * @date 2021/8/21 16:09
 */
@Service
public class ResultsServiceImpl implements ResultsService {
    private final Logger logger = LoggerFactory.getLogger(ResultsServiceImpl.class);
    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    @Autowired
    private ResultsRepository resultsRepository;
    @Autowired
    private ResultDetailService resultDetailService;
    @Autowired
    private ProjectsService projectsService;
    @Autowired
    private RobotMsgTool robotMsgTool;
    @Autowired
    private TestSuitesService testSuitesService;
    @Autowired
    private ResultDetailRepository resultDetailRepository;
    @Autowired
    private TestCasesService testCasesService;

    @Override
    public Page<Results> findByProjectId(int projectId, Pageable pageable) {
        return resultsRepository.findByProjectId(projectId, pageable);
    }

    @Override
    public boolean delete(int id) {
        if (resultsRepository.existsById(id)) {
            resultsRepository.deleteById(id);
            resultDetailService.deleteByResultId(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Results findById(int id) {
        if (resultsRepository.existsById(id)) {
            return resultsRepository.findById(id).get();
        } else {
            return null;
        }
    }

    @Override
    public void save(Results results) {
        resultsRepository.saveAndFlush(results);
    }

    @Override
    public void clean(int day) {
        long timeMillis = Calendar.getInstance().getTimeInMillis();
        long time = timeMillis - day * 86400000L;
        List<Results> resultsList = resultsRepository.findByCreateTimeBefore(new Date(time));
        cachedThreadPool.execute(() -> {
            for (Results results : resultsList) {
                logger.info("清理测试报告id：" + results.getId());
                delete(results.getId());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void suiteResult(int id) {
        Results results = findById(id);
        if (results != null) {
            results.setReceiveMsgCount(results.getReceiveMsgCount() + 1);
            setStatus(results);
        }
    }

    @Override
    public JSONArray findCaseStatus(int id) {
        Results results = findById(id);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (results != null) {
            TestSuites testSuites = testSuitesService.findById(results.getSuiteId());
            if (testSuites != null) {
                JSONArray result = new JSONArray();
                List<JSONObject> caseTimes = resultDetailRepository.findTimeByResultIdGroupByCaseId(results.getId());
                List<Integer> ci = new ArrayList<>();
                for (JSONObject j : caseTimes) {
                    ci.add(j.getInteger("case_id"));
                }
                List<TestCases> testCasesList = testCasesService.findByIdIn(ci);
                List<JSONObject> statusList = resultDetailRepository.findStatusByResultIdGroupByCaseId(results.getId());
                for (TestCases testCases : testCasesList) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("case", testCases);
                    int status = 0;
                    for (int j = caseTimes.size() - 1; j >= 0; j--) {
                        if (caseTimes.get(j).getInteger("case_id") == testCases.getId()) {
                            jsonObject.put("startTime", sf.format(caseTimes.get(j).getDate("startTime")));
                            jsonObject.put("endTime", sf.format(caseTimes.get(j).getDate("endTime")));
                            caseTimes.remove(j);
                            break;
                        }
                    }
                    List<JSONObject> device = new ArrayList<>();
                    for (int i = statusList.size() - 1; i >= 0; i--) {
                        if (statusList.get(i).getInteger("case_id") == testCases.getId()) {
                            JSONObject deviceIdAndStatus = new JSONObject();
                            deviceIdAndStatus.put("deviceId", statusList.get(i).getInteger("device_id"));
                            deviceIdAndStatus.put("status", statusList.get(i).getInteger("status"));
                            if (statusList.get(i).getInteger("status") > status) {
                                status = statusList.get(i).getInteger("status");
                            }
                            device.add(deviceIdAndStatus);
                            statusList.remove(i);
                        }
                    }
                    jsonObject.put("status", status);
                    jsonObject.put("device", device);
                    result.add(jsonObject);
                }
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void subResultCount(int id) {
        Results results = findById(id);
        if (results != null) {
            results.setSendMsgCount(results.getSendMsgCount() - 1);
            setStatus(results);
        }
    }

    @Override
    public JSONObject chart(String startTime, String endTime, int projectId) {
        List<String> dateList = getBetweenDate(startTime.substring(0, 10), endTime.substring(0, 10));
        JSONObject result = new JSONObject();
        result.put("case", resultDetailRepository.findTopCases(startTime, endTime, projectId));
        result.put("device", resultDetailRepository.findTopDevices(startTime, endTime, projectId));
        List<JSONObject> rateList = resultsRepository.findDayPassRate(startTime, endTime, projectId);
        List<JSONObject> rateResult = new ArrayList<>();
        for (String date : dateList) {
            JSONObject d = new JSONObject();
            d.put("date", date);
            d.put("rate", 0);
            for (Iterator<JSONObject> ite = rateList.iterator(); ite.hasNext(); ) {
                JSONObject s = ite.next();
                if (s.getString("date").equals(date)) {
                    d.put("rate", s.getFloat("rate"));
                    ite.remove();
                    break;
                }
            }
            rateResult.add(d);
        }
        result.put("pass", rateResult);
        result.put("status", resultsRepository.findDayStatus(startTime, endTime, projectId));
        return result;
    }

    @Override
    public void sendDayReport() {
        long timeMillis = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Projects> projectsList = projectsService.findAll();
        for (Projects projects : projectsList) {
            Date yesterday = new Date(timeMillis - 86400000);
            Date today = new Date(timeMillis);
            List<JSONObject> status = resultsRepository.findDayStatus(sf.format(yesterday), sf.format(today), projects.getId());
            int suc = 0;
            int warn = 0;
            int fail = 0;
            for (JSONObject j : status) {
                switch (j.getInteger("status")) {
                    case 1:
                        suc += j.getInteger("total");
                        break;
                    case 2:
                        warn += j.getInteger("total");
                        break;
                    case 3:
                        fail += j.getInteger("total");
                        break;
                }
            }
            if (projects.getRobotType() != 0 && projects.getRobotToken().length() > 0 && projects.getRobotSecret().length() > 0) {
                robotMsgTool.sendDayReportMessage(projects.getRobotToken(), projects.getRobotSecret(), projects.getId()
                        , projects.getProjectName(), sf.format(yesterday), sf.format(today), suc, warn, fail, projects.getRobotType());
            }
        }
    }

    @Override
    public void sendWeekReport() {
        long timeMillis = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Projects> projectsList = projectsService.findAll();
        for (Projects projects : projectsList) {
            Date lastWeek = new Date(timeMillis - 86400000 * 7L);
            Date today = new Date(timeMillis);
            List<JSONObject> status = resultsRepository.findDayStatus(sf.format(lastWeek), sf.format(today), projects.getId());
            int count = resultsRepository.findRunCount(sf.format(lastWeek), sf.format(today), projects.getId());
            int suc = 0;
            int warn = 0;
            int fail = 0;
            for (JSONObject j : status) {
                switch (j.getInteger("status")) {
                    case 1:
                        suc += j.getInteger("total");
                        break;
                    case 2:
                        warn += j.getInteger("total");
                        break;
                    case 3:
                        fail += j.getInteger("total");
                        break;
                }
            }
            if (projects.getRobotType() != 0 && projects.getRobotToken().length() > 0 && projects.getRobotSecret().length() > 0) {
                robotMsgTool.sendWeekReportMessage(projects.getRobotToken(), projects.getRobotSecret(), projects.getId()
                        , projects.getProjectName(), sf.format(lastWeek), sf.format(today), suc, warn, fail, count, projects.getRobotType());
            }
        }
    }

    public static List<String> getBetweenDate(String begin, String end) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<String> betweenList = new ArrayList<String>();

        try {
            Calendar startDay = Calendar.getInstance();
            startDay.setTime(format.parse(begin));
            startDay.add(Calendar.DATE, -1);

            while (true) {
                startDay.add(Calendar.DATE, 1);
                Date newDate = startDay.getTime();
                String newend = format.format(newDate);
                betweenList.add(newend);
                if (end.equals(newend)) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return betweenList;
    }

    public void setStatus(Results results) {
        List<ResultDetail> resultDetailList = resultDetailService.findAll(results.getId(), 0, "status", 0);
        int failCount = 0;
        int sucCount = 0;
        int warnCount = 0;
        int status;
        for (ResultDetail resultDetail : resultDetailList) {
            if (resultDetail.getStatus() == ResultDetailStatus.FAIL) {
                failCount++;
            } else if (resultDetail.getStatus() == ResultDetailStatus.WARN) {
                warnCount++;
            } else {
                sucCount++;
            }
        }
        if (failCount > 0) {
            status = ResultStatus.FAIL;
        } else if (warnCount > 0) {
            status = ResultStatus.WARNING;
        } else {
            status = ResultStatus.PASS;
        }
        //状态赋予等级最高的
        results.setStatus(status > results.getStatus() ? status : results.getStatus());
        if (results.getSendMsgCount() < 1 && sucCount == 0 && failCount == 0 && warnCount == 0) {
            delete(results.getId());
        } else {
            save(results);
            //发收相同的话，表明测试结束了
            if (results.getReceiveMsgCount() == results.getSendMsgCount()) {
                results.setEndTime(new Date());
                save(results);
                Projects projects = projectsService.findById(results.getProjectId());
                if (projects != null && projects.getRobotType() != 0 && projects.getRobotToken().length() > 0 && projects.getRobotSecret().length() > 0) {
                    robotMsgTool.sendResultFinishReport(projects.getRobotToken(), projects.getRobotSecret(),
                            results.getSuiteName(), sucCount, warnCount, failCount, projects.getId(), results.getId(), projects.getRobotType());
                }
            }
        }
    }
}
