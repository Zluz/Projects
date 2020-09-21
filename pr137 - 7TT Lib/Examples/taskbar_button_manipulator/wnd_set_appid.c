#include "stdafx.h"
#include "wnd_set_appid.h"

BOOL WndSetAppId(HWND hWnd, const WCHAR *pAppId)
{
	IPropertyStore *pps;
	PROPVARIANT pv;
	HRESULT hr;

	hr = SHGetPropertyStoreForWindow(hWnd, &IID_IPropertyStore, (void **)&pps);
	if(SUCCEEDED(hr))
	{
		if(pAppId)
		{
			pv.vt = VT_LPWSTR;
			hr = SHStrDup(pAppId, &pv.pwszVal);
		}
		else
			PropVariantInit(&pv);

		if(SUCCEEDED(hr))
		{
			hr = pps->lpVtbl->SetValue(pps, &PKEY_AppUserModel_ID, &pv);
			if(SUCCEEDED(hr))
				hr = pps->lpVtbl->Commit(pps);

			PropVariantClear(&pv);
		}

		pps->lpVtbl->Release(pps);
	}

	return SUCCEEDED(hr);
}
