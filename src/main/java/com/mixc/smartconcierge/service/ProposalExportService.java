package com.mixc.smartconcierge.service;

import com.alibaba.excel.EasyExcel;
import com.mixc.smartconcierge.entity.Proposal;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProposalExportService {

    private final ProposalService proposalService;

    public void exportOverThreshold(HttpServletResponse response) throws IOException {
        List<Proposal> list = proposalService.overThresholdList();
        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("品牌名称", "提议人数", "阈值", "是否已通知"));
        for (Proposal p : list) {
            rows.add(List.of(
                    p.getBrandName(),
                    String.valueOf(p.getCount()),
                    String.valueOf(p.getThreshold()),
                    p.getNotified() != null && p.getNotified() == 1 ? "是" : "否"
            ));
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("超阈值品牌建议书", StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream()).sheet("提议报告").doWrite(rows);
    }
}
