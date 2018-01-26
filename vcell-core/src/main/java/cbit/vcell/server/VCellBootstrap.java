/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.server;

import org.vcell.util.AuthenticationException;
import org.vcell.util.DataAccessException;
import org.vcell.util.UseridIDExistsException;
import org.vcell.util.document.UserInfo;
import org.vcell.util.document.UserLoginInfo;

import cbit.vcell.message.server.bootstrap.client.RemoteProxyVCellConnectionFactory.RemoteProxyException;
/**
 * This interface was generated by a SmartGuide.
 * 
 */
public interface VCellBootstrap {
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.server.VCellConnection
 * @exception RemoteProxyException The exception description.
 */
public VCellConnection getVCellConnection(UserLoginInfo userLoginInfo) throws RemoteProxyException, DataAccessException, AuthenticationException;
/**
 * Insert the method's description here.
 * Creation date: (6/8/2006 2:50:55 PM)
 * @return java.lang.String
 */
String getVCellSoftwareVersion() throws RemoteProxyException;

public UserInfo insertUserInfo(UserInfo newUserInfo)throws RemoteProxyException, DataAccessException,UseridIDExistsException;
public void sendLostPassword(String userid) throws RemoteProxyException, DataAccessException;
}
