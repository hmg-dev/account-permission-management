/*
 Copyright (C) 2022, Martin Drößler <m.droessler@handelsblattgroup.com>
 Copyright (C) 2022, Handelsblatt GmbH

 This file is part of account-permission-management tool

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package dev.hmg.apm.db.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditLogEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "INTEGER")
	private int id;
	
	@Column(name = "date")
	private long entryTimestamp;
	
	private String action;
	
	private String user;
	
	@Column(name = "target_user")
	private String targetUser;
	
	@Column(columnDefinition = "text")
	@Type(type = "text")
	private String description;

	@Column(columnDefinition = "text")
	@Type(type = "text")
	private String comment;
	
	public int getId() {
		return id;
	}
	
	public void setId(final int id) {
		this.id = id;
	}
	
	public long getEntryTimestamp() {
		return entryTimestamp;
	}
	
	public Instant getEntryDateTime() {
		return Instant.ofEpochMilli(entryTimestamp);
	}
	
	public void setEntryTimestamp(final long entryTimestamp) {
		this.entryTimestamp = entryTimestamp;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(final String action) {
		this.action = action;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(final String user) {
		this.user = user;
	}
	
	public String getTargetUser() {
		return targetUser;
	}
	
	public void setTargetUser(final String targetUser) {
		this.targetUser = targetUser;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(final String description) {
		this.description = description;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
