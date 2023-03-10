package org.egov.win.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Builder
@ToString
public class Email {
	private String emailTo;
	private String subject;
	private String body;
	@JsonProperty("isHTML")
	private boolean isHTML;
	private Body data;
}
