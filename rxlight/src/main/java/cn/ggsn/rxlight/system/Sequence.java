package cn.ggsn.rxlight.system;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

import cn.ggsn.openrxlight.domain.BaseEntity;
import cn.ggsn.openrxlight.utils.DistributedLock;

@Data
@Table(name = "com_sequence")
@EqualsAndHashCode(callSuper = false)
@Entity
public class Sequence extends BaseEntity {

	private static final String SYS_CODE_GEN = "sys_code_gen:";
	@Column(name = "table_name")
	private String tableName;

	@Column(name = "prefix_name")
	private String prefixName;

	@Column(name = "middle_format")
	private String middleFormat;

	@Column(name = "postfix_reset_method")
	private Integer postfixResetMethod;

	@Column(name = "postfix_digit_nums")
	private Integer postfixDigitNums;

	@Column(name = "postfix_next_seqno")
	private Long postfixNextSeqno;

	@Column(name = "last_reset_value")
	private Integer lastResetValue;

	@Column(name = "created_time")
	private LocalDateTime createdTime;

	@Column(name = "updated_time")
	private LocalDateTime updatedTime;

	public static Optional<Sequence> findByTableName(String tableName) {
		return Sequence.find("tableName", tableName).firstResultOptional();
	}

	public static String generateCode(String tableName) {
		DistributedLock distributedLock = CDI.current().select(DistributedLock.class).get();
		Sequence sequence = findByTableName(tableName)
				.orElseThrow(() -> new RuntimeException("Sequence not found for table: " + tableName));
		String key = SYS_CODE_GEN + tableName;
		String uuid = cn.ggsn.openrxlight.lang.UUID.randomUUID().toString();
		distributedLock.lock(key, uuid, 20000);
		Long postfixNextSeqno = sequence.getPostfixNextSeqno();
		String middleValue = "";
		if (StringUtils.isNotEmpty(sequence.getMiddleFormat())) {
			middleValue = DateTimeFormatter.ofPattern(sequence.getMiddleFormat()).format(LocalDateTime.now());
		}
		if (sequence.getPostfixResetMethod() != 1) {
			Integer ymd = Integer.valueOf(middleValue);
			if (Objects.isNull(sequence.getLastResetValue()) || ymd > sequence.getLastResetValue()) {
				postfixNextSeqno = 1L;
				sequence.setLastResetValue(ymd);
			}
		}
		String codeFormat = sequence.getPrefixName() + middleValue + "%0" + sequence.getPostfixDigitNums() + "d";
		var code = String.format(codeFormat, postfixNextSeqno);
		sequence.setPostfixNextSeqno(postfixNextSeqno + 1);
		sequence.save();
		distributedLock.unlock(key, uuid);
		return code;
	}
}
