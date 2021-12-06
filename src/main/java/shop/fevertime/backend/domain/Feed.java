package shop.fevertime.backend.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.fevertime.backend.dto.request.FeedRequestDto;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Feed extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "feed_id")
    private Long id;

    @Column(nullable = false)
    private String contents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Feed(String contents, User user) {
        // validation 해야함
        this.contents = contents; // dto에서 받지말고 바로 string으로 변경
        this.user = user;
    }

    public void update(FeedRequestDto requestDto) {
        this.contents = requestDto.getContents();
    }

}
