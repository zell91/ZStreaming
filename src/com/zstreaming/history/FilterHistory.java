package com.zstreaming.history;

import java.time.LocalDate;

public class FilterHistory {
	
	private Type types[];
	private String query;
	private LocalDate date;
	
	public enum Type{
		URL(0),
		NAME(1),
		HOSTER(2),
		MIN_UNO_MB(3),
		UNO_MB_50_MB(3),
		CINQUANTA_MB_DUECENTOCINQUANTA_MB(3),
		DUECENTOCINQUANTA_MB_CINQUECENTO_MB(3),
		CINQUECENTO_MB_UNO_GB(3),
		GREAT_UNO_GB(3),
		SUCCESSED(4),
		FAILED(4),
		DATE(5);
		
		int value;
		
		Type(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return this.value;
		}
		
	}
	
	public FilterHistory(String query, LocalDate date, Type... types) {
		this.query = query;
		this.types = types;
		this.date = date;
		
		for(Type type : types) {
			if(type.equals(Type.DATE) && this.date == null) {
				throw new IllegalArgumentException("Non è possibile dichiarare un filtro di type DATE senza un parametro di data valido.");
			}			
		}
	}
	
	public FilterHistory(String query, Type... types) {
		this(query, null, types);
	}
		
	public FilterHistory(String query) {
		this(query, Type.URL, Type.NAME);
	}
	
	public LocalDate getDate() {
		return date;
	}
	
	public void setDate(LocalDate localDate) {
		this.date = localDate;
	}
	
	public String getQuery() {
		return this.query;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public void setTypes(Type... types) {
		this.types = types;
	}
	
	public Type[] getTypes() {
		return types;
	}
	
}
