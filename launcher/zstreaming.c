#include <stdio.h>
#include <windows.h>
#define _WIN32_WINNT 0x0500

int main(int argc, char *argv[]){
	
	HWND hWnd = GetConsoleWindow();
	ShowWindow(hWnd, SW_MINIMIZE);
	ShowWindow(hWnd, SW_HIDE);
	
	char command[] = "java -XX:MaxHeapFreeRatio=40 -XX:MinHeapFreeRatio=40 -XX:ReservedCodeCacheSize=32m -XX:ReservedCodeCacheSize=32m -Xss512k -XX:+UseG1GC -cp bin com.zstreaming.launcher.ZStreaming";
	char *args;
	
	if(argc == 2){
		if(strcmp(argv[1], "-autorun") == 0){
			args = argv[1];		
			strcat(command, " ");
			strcat(command, args);
		}
	}
	
	chdir("");
		
	system(command);
	return 0;
}
