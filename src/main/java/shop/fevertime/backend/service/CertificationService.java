package shop.fevertime.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shop.fevertime.backend.domain.Certification;
import shop.fevertime.backend.domain.Challenge;
import shop.fevertime.backend.domain.User;
import shop.fevertime.backend.dto.request.CertificationRequestDto;
import shop.fevertime.backend.dto.response.CertificationResponseDto;
import shop.fevertime.backend.repository.CertificationRepository;
import shop.fevertime.backend.repository.ChallengeRepository;
import shop.fevertime.backend.util.LocalDateTimeUtil;
import shop.fevertime.backend.util.S3Uploader;

import javax.transaction.Transactional;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final CertificationRepository certificationRepository;
    private final S3Uploader s3Uploader;
    private final ChallengeRepository challengeRepository;

    //새로운 인증 생성
    @Transactional
    public void setCertification(CertificationRequestDto requestDto, User user) throws IOException {

        // 이미지 AWS S3 업로드
        String uploadImageUrl = s3Uploader.upload(requestDto.getImg(), "certification");

        Challenge challenge = challengeRepository.findById(requestDto.getChallengeId()).orElseThrow(
                () -> new NullPointerException("해당 아이디가 존재하지 않습니다."));

        // 인증 생성
        Certification certification = new Certification(
                uploadImageUrl,
                requestDto.getContents(),
                user,
                challenge
        );
        certificationRepository.save(certification);
    }

    //특정 인증 삭제
    public void deleteCertification(Long certificationId) {
        //이미지 s3에서 삭제
        CertificationResponseDto responseDto = certificationRepository.findById(certificationId)
                .map(CertificationResponseDto::new)
                .orElseThrow(
                        () -> new NullPointerException("해당 아이디가 존재하지 않습니다."));
        String[] ar = responseDto.getImg().split("/");
        s3Uploader.delete(ar[ar.length - 1], "certification");

        certificationRepository.deleteById(certificationId);
    }

}
