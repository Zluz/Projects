#include "stdafx.h"
#include "include/TTLib.h"

void PrintButtons(HANDLE hTaskbar, HANDLE hButtonGroup)
{
	HANDLE hActiveButton = TTLib_GetActiveButton(hTaskbar);
	HANDLE hTrackedButton = TTLib_GetTrackedButton(hTaskbar);

	int nCount;
	if(TTLib_GetButtonCount(hButtonGroup, &nCount))
	{
		wprintf(L"\tButton count: %d\n", nCount);

		for(int i = 0; i < nCount; i++)
		{
			HANDLE hButton = TTLib_GetButton(hButtonGroup, i);

			wprintf(L"\t%d: %p\n", i, hButton);

			if(hButton == hActiveButton)
				wprintf(L"\t\t* This is the active button\n");

			if(hButton == hTrackedButton)
				wprintf(L"\t\t* This is the mouse-tracked button\n");

			HWND hWnd = TTLib_GetButtonWindow(hButton);

			wprintf(L"\t\tButton window handle: %p\n", hWnd);

			WCHAR szWindowTitle[256];
			GetWindowText(hWnd, szWindowTitle, 256);

			wprintf(L"\t\tButton window title text: %s\n", szWindowTitle);
		}
	}
}

void PrintButtonGroups(HANDLE hTaskbar)
{
	HANDLE hActiveButtonGroup = TTLib_GetActiveButtonGroup(hTaskbar);
	HANDLE hTrackedButtonGroup = TTLib_GetTrackedButtonGroup(hTaskbar);

	int nCount;
	if(TTLib_GetButtonGroupCount(hTaskbar, &nCount))
	{
		wprintf(L"Button group count: %d\n", nCount);

		for(int i = 0; i < nCount; i++)
		{
			HANDLE hButtonGroup = TTLib_GetButtonGroup(hTaskbar, i);

			wprintf(L"%d: %p\n", i, hButtonGroup);

			if(hButtonGroup == hActiveButtonGroup)
				wprintf(L"\t* This is the active button group\n");

			if(hButtonGroup == hTrackedButtonGroup)
				wprintf(L"\t* This is the mouse-tracked button group\n");

			RECT rcButtonGroup;
			if(!TTLib_GetButtonGroupRect(hButtonGroup, &rcButtonGroup))
				memset(&rcButtonGroup, 0, sizeof(RECT));

			wprintf(L"\tRect: (%d, %d) - (%d, %d)\n",
				rcButtonGroup.left, rcButtonGroup.top, rcButtonGroup.right, rcButtonGroup.bottom);

			TTLIB_GROUPTYPE nButtonGroupType;
			if(!TTLib_GetButtonGroupType(hButtonGroup, &nButtonGroupType))
				nButtonGroupType = TTLIB_GROUPTYPE_UNKNOWN;

			wprintf(L"\tType: %d\n", nButtonGroupType);

			WCHAR szAppId[MAX_APPID_LENGTH];
			TTLib_GetButtonGroupAppId(hButtonGroup, szAppId, MAX_APPID_LENGTH);

			wprintf(L"\tAppId: %s\n", szAppId);

			PrintButtons(hTaskbar, hButtonGroup);
		}
	}
}

void PrintTaskbarContents(HANDLE hTaskbar)
{
	wprintf(L"Task list window handle: %p\n", TTLib_GetTaskListWindow(hTaskbar));
	wprintf(L"Taskbar window handle: %p\n", TTLib_GetTaskbarWindow(hTaskbar));
	wprintf(L"Taskbar monitor handle: %p\n", TTLib_GetTaskbarMonitor(hTaskbar));

	PrintButtonGroups(hTaskbar);
}

BOOL PrintAllTaskbarsContents(void)
{
	wprintf(L"Starting taskbar manipulation...\n");

	if(!TTLib_ManipulationStart())
	{
		wprintf(L"TTLib_ManipulationStart() failed\n");
		return FALSE;
	}

	wprintf(L"\n");
	wprintf(L"Printing main taskbar contents...\n");

	HANDLE hTaskbar = TTLib_GetMainTaskbar();
	PrintTaskbarContents(hTaskbar);

	int nCount;
	if(TTLib_GetSecondaryTaskbarCount(&nCount))
	{
		for(int i = 0; i < nCount; i++)
		{
			wprintf(L"\n");
			wprintf(L"Printing secondary taskbar #%d contents...\n", i);

			hTaskbar = TTLib_GetSecondaryTaskbar(i);
			PrintTaskbarContents(hTaskbar);
		}
	}

	TTLib_ManipulationEnd();
	return TRUE;
}

int wmain(int argc, WCHAR *argv[])
{
	BOOL bSuccess = FALSE;
	DWORD dwError;

	wprintf(L"Initializing 7+ Taskbar Tweaking library...\n");

	dwError = TTLib_Init();
	if(dwError == TTLIB_OK)
	{
		wprintf(L"Loading 7+ Taskbar Tweaking library into explorer...\n");

		dwError = TTLib_LoadIntoExplorer();
		if(dwError == TTLIB_OK)
		{
			bSuccess = PrintAllTaskbarsContents();
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
