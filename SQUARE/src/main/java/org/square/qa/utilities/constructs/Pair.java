package org.square.qa.utilities.constructs;

public class Pair<T,U>{
	private T first;
	private U second;
	
	/**
	 * Constructor
	 * @param first is the contained object of type T
	 * @param second is the contained object of type U
	 */
	public Pair(T first,U second){
		this.first = first;
		this.second = second;
	}
	
	/**
	 * Get contained object of type T
	 * @return contained object of type T
	 */
	public T getFirst(){
		return this.first;
	}
	
	/**
	 * Get contained object of type U
	 * @return contained object of type U
	 */
	public U getSecond(){
		return this.second;
	}
	
	/**
	 * Put object of type T into the container
	 * @param first is of type T
	 */
	public void putFirst(T first){
		this.first = first;
	}
	
	/**
	 * Put object of type U into the container
	 * @param second is of type U
	 */
	public void putSecond(U second){
		this.second = second;
	}
	
	public boolean equals(Object obj){
		if(!(obj instanceof Pair<?,?>))
			return false;
		final Pair<?,?> other = (Pair<?,?>) obj;
		return equal(getFirst(),other.getFirst())&&equal(getSecond(),other.getSecond());
	}
	
	/**
	 * Equality on Pairs
	 * @param o1 object of type Pair<T,U>
	 * @param o2 object of type Pair<T,U>
	 * @return true is containers objects pass their respective equality
	 */
	private boolean equal(Object o1,Object o2){
		return o1==null?o2==null:o1.equals(o2);
	}
	
	/**
	 * Equality of contained object of type T
	 * @param dst is an object of type Pair<T,U> to be compared
	 * @return true if equality passes
	 */
	public boolean equalsFirst(Pair<T,U> dst){
		return first.equals(dst.getFirst());
	}
	
	/**
	 * Equality of contained object of type U
	 * @param dst is an object of type Pair<T,U> to be compared
	 * @return true if equality passes
	 */
	public boolean equalsSecond(Pair<T,U> dst){
		return second.equals(dst.getSecond());
	}
	
	/**
	 * Equality of contained object of type T
	 * @param dst is an object of type T
	 * @return true if equality passes
	 */
	public boolean equalsFirst(T dst){
		return first.equals(dst);
	}
	
	/**
	 * Equality of contained object of type U
	 * @param dst is an object of type U
	 * @return true if equality passes
	 */
	public boolean equalsSecond(U dst){
		return second.equals(dst);
	}
	
	public int hashCode(){
		int hLeft = getFirst() == null ? 0 : getFirst().hashCode();
		int hRight = getSecond() == null ? 0 : getSecond().hashCode();
		return hLeft + (37*hRight);
	}
	
	public String toString(){
		return getFirst().toString()+" "+getSecond().toString();
	}
	
	@SuppressWarnings("unchecked")
	public int compareTo(Pair<T,U> other){
//		return ((Comparable<U>)this.getSecond()).compareTo(other.getSecond()); //ascending
		return ((Comparable<U>)other.getSecond()).compareTo(this.getSecond()); //decending
	}
}
