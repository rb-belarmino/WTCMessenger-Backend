package com.wtcmessenger.dto;

import java.util.List;
import java.util.Map;

public record CampaignResponse(
    String title,
    String body,
    String url,
    List<CampaignAction> actions,
    Map<String, String> actionUrls
) {
    public record CampaignAction(
        String action,
        String title
    ) {}
}
