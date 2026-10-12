package app.lovable.luckyvpnmaster.rewards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import app.lovable.luckyvpnmaster.R;
import app.lovable.luckyvpnmaster.auth.AuthManager;

/**
 * Lucky wheel dialog: spin once per hour for 10–1000 points.
 * Spin result comes from POST /rewards/wheel; the wheel animation is visual.
 */
public class LuckyWheelDialog extends DialogFragment {

    public interface OnPointsEarned {
        void onPointsEarned(int points);
    }

    private LuckyWheelView wheelView;
    private Button btnSpin;
    private TextView tvStatus;
    private RewardsManager rewardsManager;
    private AuthManager authManager;
    private OnPointsEarned listener;

    public static LuckyWheelDialog newInstance() {
        return new LuckyWheelDialog();
    }

    public void setOnPointsEarnedListener(OnPointsEarned listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_lucky_wheel, container, false);
        rewardsManager = new RewardsManager(requireContext());
        authManager = new AuthManager(requireContext());

        wheelView = view.findViewById(R.id.wheel_view);
        btnSpin = view.findViewById(R.id.btn_spin);
        tvStatus = view.findViewById(R.id.tv_wheel_status);

        btnSpin.setOnClickListener(v -> onSpinClicked());
        updateCooldownText();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void onSpinClicked() {
        if (!authManager.isLoggedIn()) {
            dismiss();
            return;
        }
        if (wheelView.isSpinning()) return;

        long remaining = rewardsManager.getWheelCooldownRemainingMs();
        if (remaining > 0) {
            updateCooldownText();
            return;
        }

        btnSpin.setEnabled(false);
        tvStatus.setText(R.string.wheel_spinning);

        rewardsManager.wheelSpin(new RewardsManager.RewardCallback() {
            @Override
            public void onSuccess(int pointsEarned) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    rewardsManager.recordWheelSpin();
                    wheelView.spin(() -> showResult(pointsEarned));
                });
            }

            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    StringBuilder sb = new StringBuilder(error);
                    long remaining = rewardsManager.getWheelCooldownRemainingMs();
                    if (remaining > 0) {
                        sb.append("\n").append(cooldownString(remaining));
                        btnSpin.setEnabled(false);
                    } else {
                        btnSpin.setEnabled(true);
                    }
                    tvStatus.setText(sb.toString());
                });
            }
        });
    }

    /** "MM:SS" cooldown line, e.g. "Spin again in 42:10". */
    private String cooldownString(long remainingMs) {
        long totalSec = remainingMs / 1000;
        String mmss = String.format("%02d:%02d", totalSec / 60, totalSec % 60);
        return getString(R.string.wheel_cooldown, mmss);
    }

    private void updateCooldownText() {
        if (tvStatus == null || rewardsManager == null || btnSpin == null) return;
        long remaining = rewardsManager.getWheelCooldownRemainingMs();
        if (remaining > 0) {
            tvStatus.setText(cooldownString(remaining));
            btnSpin.setEnabled(false);
        } else {
            tvStatus.setText("");
            btnSpin.setEnabled(!wheelView.isSpinning());
        }
    }

    private void showResult(int points) {
        if (getActivity() == null) return;
        btnSpin.setEnabled(false);
        tvStatus.setText(getString(R.string.wheel_result_points, points));
        updateCooldownText();
        if (listener != null) listener.onPointsEarned(points);

        new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.wheel_result_title)
                .setMessage(getString(R.string.wheel_result_points, points))
                .setPositiveButton(R.string.ok, (d, w) -> {
                    // after the user closes the result, also close the wheel dialog
                    dismiss();
                })
                .setCancelable(true)
                .show();
    }
}
