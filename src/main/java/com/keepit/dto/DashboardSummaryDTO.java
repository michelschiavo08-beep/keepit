package com.keepit.dto;

import java.util.List;

public class DashboardSummaryDTO {

    private long totalAssets;
    private long okAssetsCount;
    private long alertAssetsCount;
    private List<AssetDTO> okAssets;
    private List<AssetDTO> alertAssets;

    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(long totalAssets, long okAssetsCount, long alertAssetsCount, List<AssetDTO> okAssets, List<AssetDTO> alertAssets) {
        this.totalAssets = totalAssets;
        this.okAssetsCount = okAssetsCount;
        this.alertAssetsCount = alertAssetsCount;
        this.okAssets = okAssets;
        this.alertAssets = alertAssets;
    }

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public long getOkAssetsCount() {
        return okAssetsCount;
    }

    public void setOkAssetsCount(long okAssetsCount) {
        this.okAssetsCount = okAssetsCount;
    }

    public long getAlertAssetsCount() {
        return alertAssetsCount;
    }

    public void setAlertAssetsCount(long alertAssetsCount) {
        this.alertAssetsCount = alertAssetsCount;
    }

    public List<AssetDTO> getOkAssets() {
        return okAssets;
    }

    public void setOkAssets(List<AssetDTO> okAssets) {
        this.okAssets = okAssets;
    }

    public List<AssetDTO> getAlertAssets() {
        return alertAssets;
    }

    public void setAlertAssets(List<AssetDTO> alertAssets) {
        this.alertAssets = alertAssets;
    }
}
