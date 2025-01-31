package quyet.learn.spring.resporitory;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import quyet.learn.spring.entity.InvalidatedToken;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    /**
     * Xóa tất cả các token có `expiryTime` trước thời gian hiện tại.
     *
     * @param expiryTime Thời gian hết hạn.
     * @return Số lượng bản ghi đã xóa.
     */
    int deleteByExpiryTimeBefore(Date expiryTime);
}
