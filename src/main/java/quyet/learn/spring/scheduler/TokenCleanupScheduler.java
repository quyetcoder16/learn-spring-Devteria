package quyet.learn.spring.scheduler;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import quyet.learn.spring.resporitory.InvalidatedTokenRepository;

import java.util.Date;

@Component
public class TokenCleanupScheduler {

    @Autowired
    private InvalidatedTokenRepository invalidatedTokenRepository;

    /**
     * Cron job chạy hàng ngày lúc 00:00 để xóa các token hết hạn.
     */
   // @Scheduled(cron = "0 19 18 * * ?") // Chạy lúc 6:15 PM mỗi ngày
    @Scheduled(cron = "0 0 0 * * ?")// Cấu hình cron chạy lúc 00:00 mỗi ngày
    @Transactional // Đảm bảo phương thức này chạy trong một transaction
    public void cleanExpiredTokens() {
        Date now = new Date();
        int deletedCount = invalidatedTokenRepository.deleteByExpiryTimeBefore(now);

        System.out.println("Deleted " + deletedCount + " expired tokens at " + now);
    }
}
