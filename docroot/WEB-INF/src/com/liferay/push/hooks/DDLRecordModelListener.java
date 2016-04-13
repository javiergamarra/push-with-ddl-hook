/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.push.hooks;

import com.liferay.portal.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.BaseModelListener;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.dynamicdatalists.model.DDLRecord;
import com.liferay.pushnotifications.service.PushNotificationsDeviceLocalServiceUtil;

import java.util.List;

/**
 * Created by aelian on 29/05/15.
 */
public class DDLRecordModelListener extends BaseModelListener<DDLRecord> {

	public static final int MAX_USERS = 200;

	@Override
	public void onAfterCreate(DDLRecord model) throws ModelListenerException {
		try {
			LogFactoryUtil.getLog("a").error("asdas", null);
			super.onAfterCreate(model);

			sendNotification(model, true);
		} catch (PortalException e) {
			e.printStackTrace();
		}
		catch (SystemException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAfterUpdate(DDLRecord model) throws ModelListenerException {
		try {
			LogFactoryUtil.getLog("a").error("asdas", null);
			super.onAfterUpdate(model);

			sendNotification(model, false);
		} catch (PortalException e) {
			e.printStackTrace();
		}
		catch (SystemException e) {
			e.printStackTrace();
		}
	}

	private void sendNotification(DDLRecord model, boolean newNotification) throws PortalException, SystemException {
		JSONObject jsonObject = JSONFactoryUtil.createJSONObject();
		jsonObject.put("model", model.getPrimaryKey());
		jsonObject.put("title", (String) model.getFieldValue("Title"));
		jsonObject.put("description", (String) model.getFieldValue("Description"));
		jsonObject.put("newNotification", newNotification);
		jsonObject.put("photo", (String) model.getFieldValue("Photo"));


		//FIXME find by token instead of users
		List<User> users = findSomeUsers(0, MAX_USERS);

		long[] ids = getPrimaryKeysFromAList(users);

		PushNotificationsDeviceLocalServiceUtil.sendPushNotification(ids, jsonObject);
	}

	private List<User> findSomeUsers(int from, int to) throws PortalException, SystemException {
		Company company = CompanyLocalServiceUtil.getCompanyByMx(PropsUtil.get(PropsKeys.COMPANY_DEFAULT_WEB_ID));
		long companyId = company.getCompanyId();
		return UserLocalServiceUtil.getCompanyUsers(companyId, from, to);
	}

	private long[] getPrimaryKeysFromAList(List<User> users) {
		long[] ids = new long[users.size()];

		for (int i = 0; i < users.size(); i++) {
			ids[i] = users.get(i).getPrimaryKey();
		}
		return ids;
	}
}
