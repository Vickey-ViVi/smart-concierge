package com.mixc.smartconcierge.controller.api;

import com.mixc.smartconcierge.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proposal")
@RequiredArgsConstructor
public class ProposalApiController {

    private final ProposalService proposalService;

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody Map<String, String> body,
                                   @RequestHeader(value = "X-Openid", required = false) String openid) {
        return proposalService.addProposal(body.get("brand"), body.get("reason"), openid);
    }

    @GetMapping("/hot")
    public List<Map<String, Object>> hot() {
        return proposalService.hotProposals();
    }

    @PostMapping("/like")
    public Map<String, Object> like(@RequestBody Map<String, String> body) {
        return proposalService.likeProposal(body.get("brand"));
    }
}
