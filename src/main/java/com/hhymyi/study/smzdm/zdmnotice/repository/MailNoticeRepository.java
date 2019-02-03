package com.hhymyi.study.smzdm.zdmnotice.repository;

import com.hhymyi.study.smzdm.zdmnotice.entity.MailNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MailNoticeRepository extends JpaRepository<MailNotice,Long>, JpaSpecificationExecutor<MailNotice> {

	Optional<MailNotice> findByArticleId(Long articleId);

//	Page<MailNotice> findByParam(@Param("noticeDate") String noticeDate,@Param("searchContent") String searchContent, Pageable pageable);
}