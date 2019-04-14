package com.lucid.lwx.license;

public class ExpiredLicenseException
  extends RuntimeException
{
  public ExpiredLicenseException(String message)
  {
    super(message);
  }
}


