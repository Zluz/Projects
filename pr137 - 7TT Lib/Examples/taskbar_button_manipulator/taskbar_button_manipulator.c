#include "stdafx.h"
#include "wnd_set_appid.h"
#include "include/TTLib.h"
#include "resource.h"

static const WCHAR *pszProcessApplicationId = L"taskbar_button_manipulator";

void OnRadioButtonClick(int nCtrlId, HWND hWnd)
{
	TTLIB_LIST List;
	BOOL bAddToList = TRUE;
	TTLIB_LIST_VALUE ListValue;
	BOOL bSucceeded;

	switch(nCtrlId)
	{
	case IDC_LABELS_N:
	case IDC_LABELS_A:
	case IDC_LABELS_D:
		List = TTLIB_LIST_LABEL;
		break;

	case IDC_GROUP_N:
	case IDC_GROUP_A:
	case IDC_GROUP_D:
		List = TTLIB_LIST_GROUP;
		break;

	case IDC_GROUP_PINNED_N:
	case IDC_GROUP_PINNED_A:
	case IDC_GROUP_PINNED_D:
		List = TTLIB_LIST_GROUPPINNED;
		break;

	case IDC_COMBINE_N:
	case IDC_COMBINE_A:
	case IDC_COMBINE_D:
		List = TTLIB_LIST_COMBINE;
		break;
	}

	switch(nCtrlId)
	{
	case IDC_LABELS_N:
		ListValue = TTLIB_LIST_LABEL_NEVER;
		break;

	case IDC_LABELS_A:
		ListValue = TTLIB_LIST_LABEL_ALWAYS;
		break;

	case IDC_GROUP_N:
		ListValue = TTLIB_LIST_GROUP_NEVER;
		break;

	case IDC_GROUP_A:
		ListValue = TTLIB_LIST_GROUP_ALWAYS;
		break;

	case IDC_GROUP_PINNED_N:
		ListValue = TTLIB_LIST_GROUPPINNED_NEVER;
		break;

	case IDC_GROUP_PINNED_A:
		ListValue = TTLIB_LIST_GROUPPINNED_ALWAYS;
		break;

	case IDC_COMBINE_N:
		ListValue = TTLIB_LIST_COMBINE_NEVER;
		break;

	case IDC_COMBINE_A:
		ListValue = TTLIB_LIST_COMBINE_ALWAYS;
		break;

	case IDC_LABELS_D:
	case IDC_GROUP_D:
	case IDC_GROUP_PINNED_D:
	case IDC_COMBINE_D:
		bAddToList = FALSE;
		break;
	}

	if(bAddToList)
	{
		wprintf(L"Adding AppId to list... ");
		bSucceeded = TTLib_AddAppIdToList(List, pszProcessApplicationId, ListValue);
		wprintf(L"Operation %s\n", bSucceeded ? L"succeeded" : L"failed");
	}
	else
	{
		wprintf(L"Removing AppId from list... ");
		bSucceeded = TTLib_RemoveAppIdFromList(List, pszProcessApplicationId);
		wprintf(L"Operation %s\n", bSucceeded ? L"succeeded" : L"failed");
	}
}

void InitializeDialog(HWND hWnd)
{
	WndSetAppId(hWnd, pszProcessApplicationId);

	CheckDlgButton(hWnd, IDC_LABELS_D, BST_CHECKED);
	CheckDlgButton(hWnd, IDC_GROUP_D, BST_CHECKED);
	CheckDlgButton(hWnd, IDC_GROUP_PINNED_D, BST_CHECKED);
	CheckDlgButton(hWnd, IDC_COMBINE_D, BST_CHECKED);
}

INT_PTR CALLBACK DialogProc(
	_In_  HWND hWnd,
	_In_  UINT uMsg,
	_In_  WPARAM wParam,
	_In_  LPARAM lParam
	)
{
	switch(uMsg)
	{
	case WM_INITDIALOG:
		InitializeDialog(hWnd);
		break;

	case WM_COMMAND:
		switch(LOWORD(wParam))
		{
		case IDOK:
			break;

		case IDCANCEL:
			DestroyWindow(hWnd);
			break;

		default:
			if(LOWORD(wParam) >= IDC_LABELS_N && LOWORD(wParam) <= IDC_COMBINE_D)
				OnRadioButtonClick(LOWORD(wParam), (HWND)lParam);
			break;
		}
		break;

	case WM_DESTROY:
		break;
	}

	return FALSE;
}

int wmain(int argc, WCHAR *argv[])
{
	BOOL bSuccess = FALSE;
	DWORD dwError;

	WndSetAppId(GetConsoleWindow(), pszProcessApplicationId);

	wprintf(L"Initializing 7+ Taskbar Tweaking library...\n");

	dwError = TTLib_Init();
	if(dwError == TTLIB_OK)
	{
		wprintf(L"Loading 7+ Taskbar Tweaking library into explorer...\n");

		dwError = TTLib_LoadIntoExplorer();
		if(dwError == TTLIB_OK)
		{
			if(DialogBox(GetModuleHandle(NULL), MAKEINTRESOURCE(IDD_DIALOG1), NULL, DialogProc) != -1)
				bSuccess = TRUE;

			TTLib_UnloadFromExplorer();
		}
		else
			wprintf(L"TTLib_LoadIntoExplorer() failed with error %u\n", dwError);

		TTLib_Uninit();
	}
	else
		wprintf(L"TTLib_Init() failed with error %u\n", dwError);

	return bSuccess ? 0 : 1;
}
