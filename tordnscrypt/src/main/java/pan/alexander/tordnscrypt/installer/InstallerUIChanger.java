package pan.alexander.tordnscrypt.installer;

/*
    This file is part of InviZible Pro.

    InviZible Pro is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    InviZible Pro is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with InviZible Pro.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2019-2020 by Garmatin Oleksandr invizible.soft@gmail.com
*/

import androidx.drawerlayout.widget.DrawerLayout;
import android.util.Log;

import pan.alexander.tordnscrypt.DNSCryptRunFragment;
import pan.alexander.tordnscrypt.ITPDRunFragment;
import pan.alexander.tordnscrypt.MainActivity;
import pan.alexander.tordnscrypt.R;
import pan.alexander.tordnscrypt.TorRunFragment;
import pan.alexander.tordnscrypt.dialogs.DialogAfterInstallation;

import static pan.alexander.tordnscrypt.utils.RootExecService.LOG_TAG;

class InstallerUIChanger {
    private MainActivity mainActivity;
    private DNSCryptRunFragment dnsCryptRunFragment;
    private TorRunFragment torRunFragment;
    private ITPDRunFragment itpdRunFragment;

    InstallerUIChanger(MainActivity mainActivity) {
        getViews(mainActivity);
    }

    private void getViews(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        if (mainActivity == null) {
            throw new IllegalStateException("Installer: InstallerUIChanger is null, interrupt installation");
        }

        dnsCryptRunFragment = mainActivity.getDNSCryptRunFragment();
        torRunFragment = mainActivity.getTorRunFragment();
        itpdRunFragment = mainActivity.getITPDRunFragment();

        Log.i(LOG_TAG, "Installer: getViews() OK");
    }

    Runnable lockDrawerMenu(final boolean lock) {
       return () -> {
           DrawerLayout mDrawerLayout = mainActivity.findViewById(R.id.drawer_layout);
           if (lock) {
               mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
               Log.i(LOG_TAG, "Installer: DrawerMenu locked");
           } else {
               mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
               Log.i(LOG_TAG, "Installer: DrawerMenu unlocked");
           }
       };
    }

    Runnable setModulesStatusTextInstalling() {
        return () -> {
            if (dnsCryptRunFragment != null) {
                dnsCryptRunFragment.setDNSCryptStatus(R.string.tvDNSInstalling, R.color.textModuleStatusColorInstalling);
            }

            if (torRunFragment != null) {
                torRunFragment.setTorStatus(R.string.tvTorInstalling, R.color.textModuleStatusColorInstalling);
            }

            if (itpdRunFragment != null) {
                itpdRunFragment.setITPDStatus(R.string.tvITPDInstalling, R.color.textModuleStatusColorInstalling);
            }

            Log.i(LOG_TAG, "Installer: setModulesStatusTextInstalling");
        };
    }

    Runnable setDnsCryptInstalledStatus() {
        return () -> dnsCryptRunFragment.setDNSCryptStatus(R.string.tvDNSInstalled, R.color.textModuleStatusColorInstalled);
    }

    Runnable setTorInstalledStatus() {
        return () -> torRunFragment.setTorStatus(R.string.tvTorInstalled, R.color.textModuleStatusColorInstalled);
    }

    Runnable setItpdInstalledStatus() {
        return () -> itpdRunFragment.setITPDStatus(R.string.tvITPDInstalled, R.color.textModuleStatusColorInstalled);
    }

    Runnable setModulesStartButtonsDisabled() {
        return () -> {
            if (dnsCryptRunFragment != null) {
                dnsCryptRunFragment.setStartButtonEnabled(false);
            }

            if (torRunFragment != null) {
                torRunFragment.setStartButtonEnabled(false);
            }

            if (itpdRunFragment != null) {
                itpdRunFragment.setStartButtonEnabled(false);
            }

            Log.i(LOG_TAG, "Installer: setModulesStartButtonsDisabled");
        };
    }

    Runnable startModulesProgressBarIndeterminate() {
        return () -> {
            if (dnsCryptRunFragment != null) {
                dnsCryptRunFragment.setProgressBarIndeterminate(true);
            }

            if (torRunFragment != null) {
                torRunFragment.setProgressBarIndeterminate(true);
            }

            if (itpdRunFragment != null) {
                itpdRunFragment.setProgressBarIndeterminate(true);
            }

            Log.i(LOG_TAG, "Installer: startModulesProgressBarIndeterminate");
        };
    }

    Runnable stopModulesProgressBarIndeterminate() {
        return () -> {
            if (dnsCryptRunFragment != null) {
                dnsCryptRunFragment.setProgressBarIndeterminate(false);
            }

            if (torRunFragment != null) {
                torRunFragment.setProgressBarIndeterminate(false);
            }

            if (itpdRunFragment != null) {
                itpdRunFragment.setProgressBarIndeterminate(false);
            }

            Log.i(LOG_TAG, "Installer: stopModulesProgressBarIndeterminate");
        };
    }

    Runnable dnsCryptProgressBarIndeterminate(final boolean indeterminate) {
        return () -> {
            if (dnsCryptRunFragment != null) {
                dnsCryptRunFragment.setProgressBarIndeterminate(indeterminate);
            }
        };
    }

    Runnable torProgressBarIndeterminate(final boolean indeterminate) {
        return () -> {
            if (torRunFragment != null) {
                torRunFragment.setProgressBarIndeterminate(indeterminate);
            }
        };
    }
    Runnable itpdProgressBarIndeterminate(final boolean indeterminate) {
        return () -> {
            if (itpdRunFragment != null) {
                itpdRunFragment.setProgressBarIndeterminate(indeterminate);
            }
        };
    }


    Runnable setModulesStartButtonsEnabled() {
        return () -> {
            if (dnsCryptRunFragment != null) {
                dnsCryptRunFragment.setStartButtonEnabled(true);
            }

            if (torRunFragment != null) {
                torRunFragment.setStartButtonEnabled(true);
            }

            if (itpdRunFragment != null) {
                itpdRunFragment.setStartButtonEnabled(true);
            }

            Log.i(LOG_TAG, "Installer: setModulesStartButtonsEnabled");
        };
    }

    Runnable showDialogAfterInstallation() {
        return () -> DialogAfterInstallation.getDialogBuilder(mainActivity).show();
    }

    Runnable setModulesStatusTextError() {
        return () -> {
            if (dnsCryptRunFragment != null) {
                dnsCryptRunFragment.setDNSCryptStatus(R.string.wrong, R.color.textModuleStatusColorAlert);
            }

            if (torRunFragment != null) {
                torRunFragment.setTorStatus(R.string.wrong, R.color.textModuleStatusColorAlert);
            }

            if (itpdRunFragment != null) {
                itpdRunFragment.setITPDStatus(R.string.wrong, R.color.textModuleStatusColorAlert);
            }

            Log.i(LOG_TAG, "Installer: setModulesStatusTextError");
        };
    }

    void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }
}
