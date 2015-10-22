/**
 * @author haoxy
 * @createdate 2013年9月13日 上午9:37:03
 * @description 
 */
package com.yutong.axxc.parents.entity.distance;

import java.io.Serializable;

/**
 * @author haoxy
 *
 */
public class Site implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 328415592346294578L;
	
	private String site_name;
	private String site_remark;
	private String site_longitude;
	private String site_latitude;
	private int control_station = 0;//0:up,1:down
	private int site_Id = 0;
	private int trip_Id = 0;
	
	public String getSite_name()
	{
		return site_name;
	}
	public void setSite_name(String site_name)
	{
		this.site_name = site_name;
	}
	public String getSite_remark()
	{
		return site_remark;
	}
	public void setSite_remark(String site_remark)
	{
		this.site_remark = site_remark;
	}
	public String getSite_longitude()
	{
		return site_longitude;
	}
	public void setSite_longitude(String site_longitude)
	{
		this.site_longitude = site_longitude;
	}
	public String getSite_latitude()
	{
		return site_latitude;
	}
	public void setSite_latitude(String site_latitude)
	{
		this.site_latitude = site_latitude;
	}
	public int getControl_station()
	{
		return control_station;
	}
	public void setControl_station(int control_station)
	{
		this.control_station = control_station;
	}
	public int getSite_Id()
	{
		return site_Id;
	}
	public void setSite_Id(int site_Id)
	{
		this.site_Id = site_Id;
	}
	public int getTrip_Id()
	{
		return trip_Id;
	}
	public void setTrip_Id(int trip_Id)
	{
		this.trip_Id = trip_Id;
	}
}