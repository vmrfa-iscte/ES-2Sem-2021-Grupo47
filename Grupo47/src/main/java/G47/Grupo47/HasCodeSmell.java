package G47.Grupo47;

public class HasCodeSmell {
	private String method_name;
	private String hasCodeSmell;
	private String method_id;
	private String package_name;
	private String class_name;

	public HasCodeSmell(String name, String hasCodeSmell,String method_id, String package_name, String class_name) {
		this.method_name = name;
		this.hasCodeSmell = hasCodeSmell;
		this.method_id = method_id;
		this.package_name = package_name;
		this.class_name = class_name;
	}
	
	public String getMethod_ID() {
		return method_id;
	}

	public String getMethodName() {
		return method_name;
	}

	public String getHasCodeSmell() {
		return hasCodeSmell;
	}

}
