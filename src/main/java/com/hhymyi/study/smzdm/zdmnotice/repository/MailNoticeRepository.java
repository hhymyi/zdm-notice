package com.hhymyi.study.smzdm.zdmnotice.repository;

import com.hhymyi.study.smzdm.zdmnotice.entity.MailNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailNoticeRepository extends JpaRepository<MailNotice,Long> {

	Optional<MailNotice> findByArticleId(Long articleId);
}
