package br.com.anteros.mobile.core.synchronism.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import br.com.anteros.persistence.metadata.annotation.Cascade;
import br.com.anteros.persistence.metadata.annotation.Column;
import br.com.anteros.persistence.metadata.annotation.DiscriminatorColumn;
import br.com.anteros.persistence.metadata.annotation.Entity;
import br.com.anteros.persistence.metadata.annotation.Fetch;
import br.com.anteros.persistence.metadata.annotation.ForeignKey;
import br.com.anteros.persistence.metadata.annotation.GeneratedValue;
import br.com.anteros.persistence.metadata.annotation.Id;
import br.com.anteros.persistence.metadata.annotation.Inheritance;
import br.com.anteros.persistence.metadata.annotation.OrderBy;
import br.com.anteros.persistence.metadata.annotation.SequenceGenerator;
import br.com.anteros.persistence.metadata.annotation.Table;
import br.com.anteros.persistence.metadata.annotation.type.CascadeType;
import br.com.anteros.persistence.metadata.annotation.type.FetchMode;
import br.com.anteros.persistence.metadata.annotation.type.FetchType;
import br.com.anteros.persistence.metadata.annotation.type.GeneratedType;
import br.com.anteros.persistence.metadata.annotation.type.InheritanceType;

@SuppressWarnings("serial")
@Entity
@Table(name = "MOBILE_OBJETO")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TP_OBJETO", length = 20)
public abstract class Synchronism implements ISynchronism, Serializable, Cloneable {

	@Id
	@Column(name = "ID_OBJETO")
	@GeneratedValue(strategy = GeneratedType.AUTO)
	@SequenceGenerator(sequenceName = "SEQ_MOBILE_OBJETO", initialValue = 1)
	private Long id;

	@Column(name = "NOME_OBJETO", length = 100, required = true)
	private String name;

	@Column(name = "DS_OBJETO", length = 200)
	private String description;

	@ForeignKey(type = FetchType.EAGER)
	@Column(name = "ID_OBJETO_PAI", inversedColumn = "ID_OBJETO")
	private Synchronism objectOwner;

	@OrderBy(clause = "SEQUENCE_PARAMETER")
	@Fetch(type = FetchType.LAZY, mode = FetchMode.ONE_TO_MANY, mappedBy = "objectOwner")
	@Cascade(values = { CascadeType.SAVE })
	private Set<Synchronism> items;

	@Column(name = "BO_ATIVO", length = 1, defaultValue = "S", required = true)
	private String active;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Synchronism getObjectOwner() {
		return objectOwner;
	}

	public void setObjectOwner(Synchronism objectOwner) {
		this.objectOwner = objectOwner;
	}

	public Set<Synchronism> getItems() {
		return items;
	}

	public void setItems(Set<Synchronism> items) {
		this.items = items;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Synchronism clone = (Synchronism) super.clone();
		clone.setId(null);
		Set<Synchronism> cloneItens = new HashSet<Synchronism>();
		for (Synchronism sync : getItems()) {
			Synchronism itemClone = (Synchronism) sync.clone();
			itemClone.setObjectOwner(clone);
			cloneItens.add(itemClone);
		}
		clone.setItems(cloneItens);
		return clone;
	}
}
