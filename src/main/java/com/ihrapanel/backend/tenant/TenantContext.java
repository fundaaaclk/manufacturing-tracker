package com.ihrapanel.backend.tenant;

import java.util.UUID;

public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_COMPANY = new ThreadLocal<>();

    private TenantContext() {
        // Utility class: instance oluşturulmasını engelliyoruz. kural baska yerde  değiştirilmez
    }

    //. Bu bilinçli: tenant-owned bir işlem yanlışlıkla 
    // tenant bilgisi olmadan çalışırsa sessizce devam etmesini istemiyoruz
    public static void setCompanyId(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId cannot be null");
        }

        CURRENT_COMPANY.set(companyId);
    }

    public static UUID getCompanyId() {
        UUID companyId = CURRENT_COMPANY.get();

        if (companyId == null) {
            throw new IllegalStateException("Tenant context is not set");
        }

        return companyId;
    }

    //Login yaparken veya tenant bilgisi olmayan bir işlemde kullanmak için
    public static UUID getCompanyIdOrNull() {
        return CURRENT_COMPANY.get();
    }

    public static boolean isSet() {
        return CURRENT_COMPANY.get() != null;
    }


    //. Server thread'leri tekrar kullanabildiği için eski şirket bilgisinin sonraki request'e sızmasını istemiyoruz.
    public static void clear() {
        CURRENT_COMPANY.remove();
    }
}