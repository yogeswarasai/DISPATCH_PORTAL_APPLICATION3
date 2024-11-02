package com.iocl.Dispatch_Portal_Application.Entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.Data;


@Entity
@Table(name = "mst_role")
@Data
public class MstRole {

	@Id
    @Column(name = "role_id", length = 10)
    private String roleId;

    @Column(name = "role_desc", length = 50)
    private String roleDesc;
    
//    @ManyToMany(mappedBy = "roles")
//    private Set<MstUser> users = new HashSet<>();
}
