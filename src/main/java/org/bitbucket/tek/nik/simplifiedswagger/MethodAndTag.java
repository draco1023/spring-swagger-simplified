package org.bitbucket.tek.nik.simplifiedswagger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import io.swagger.models.Tag;

public class MethodAndTag {
	public MethodAndTag() {
		super();
		
	}
	public MethodAndTag(Method method, Tag tag, Annotation matchedRequestMapping) {
		super();
		this.method = method;
		this.tag = tag;
		this.matchedRequestMapping = matchedRequestMapping;
	}
	private Method method;
	private Tag tag;
	private Annotation matchedRequestMapping;
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public Tag getTag() {
		return tag;
	}
	public void setTag(Tag tag) {
		this.tag = tag;
	}
	public Annotation getMatchedRequestMapping() {
		return matchedRequestMapping;
	}
	public void setMatchedRequestMapping(Annotation matchedRequestMapping) {
		this.matchedRequestMapping = matchedRequestMapping;
	}
	

}
