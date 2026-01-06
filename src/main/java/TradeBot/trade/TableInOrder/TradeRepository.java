package TradeBot.trade.TableInOrder;



import TradeBot.Auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    Page<Trade> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Trade> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Trade> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}

